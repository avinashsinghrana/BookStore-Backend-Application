package com.bridgelabz.bookstore.serviceimplementation;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.bridgelabz.bookstore.dto.*;
import com.bridgelabz.bookstore.enums.RoleType;
import com.bridgelabz.bookstore.exception.BookException;
import com.bridgelabz.bookstore.model.*;
import com.bridgelabz.bookstore.repository.*;
import com.bridgelabz.bookstore.response.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.exception.UserNotFoundException;
import com.bridgelabz.bookstore.exception.UserVerificationException;
import com.bridgelabz.bookstore.service.UserService;
import com.bridgelabz.bookstore.utility.JwtGenerator;
import com.bridgelabz.bookstore.utility.RabbitMQSender;
import com.bridgelabz.bookstore.utility.RedisTempl;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import static java.util.stream.Collectors.toList;

import java.sql.Timestamp;

@Service
@PropertySource(name = "user", value = {"classpath:response.properties"})
public class UserServiceImplementation implements UserService {
    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private WishListRepository wish;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AmazonS3ClientServiceImpl amazonS3ClientService;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private RedisTempl<Object> redis;

    @Autowired
    JwtGenerator jwtop;

    private String redisKey = "Key";

    private static final long REGISTRATION_EXP = (long) 10800000;
    private static String VERIFICATION_URL;
    private static String RESETPASSWORD_URL;

    @PostConstruct
    public void setup() {
        VERIFICATION_URL = environment.getProperty("user.base.url") + "/verification/";
        RESETPASSWORD_URL = environment.getProperty("user.base.url") + "/resetpassword?token=";
    }

    @Override
    @Transactional
    public boolean register(RegistrationDto registrationDto) throws UserException {
        UserModel emailavailable = userRepository.findByEmailId(registrationDto.getEmailId());
        if (emailavailable != null) {
            return false;
        } else {
            UserModel userDetails = new UserModel();
            BeanUtils.copyProperties(registrationDto, userDetails);
            userDetails.setPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
            long id = userRepository.save(userDetails).getUserId();
            UserModel sendMail = userRepository.findByEmailId(registrationDto.getEmailId());
            String response = VERIFICATION_URL + JwtGenerator.createJWT(sendMail.getUserId(), REGISTRATION_EXP);
            System.out.println(response);
            redis.putMap(redisKey, userDetails.getEmailId(), userDetails.getFullName());
            switch (registrationDto.getRoleType()) {
                case SELLER:
                    SellerModel sellerDetails = new SellerModel();
                    sellerDetails.setSellerName(registrationDto.getFullName());
                    sellerDetails.setEmailId(registrationDto.getEmailId());
                    sellerDetails.setUserId(id);
                    sellerRepository.save(sellerDetails);
                    break;
                case ADMIN:
                    AdminModel adminDetails = new AdminModel();
                    adminDetails.setAdminName(registrationDto.getFullName());
                    adminDetails.setEmailId(registrationDto.getEmailId());
                    adminRepository.save(adminDetails);
                    break;
            }
            if (rabbitMQSender.send(new EmailObject(sendMail.getEmailId(), "Registration Link...", response, "link for Verification")))
                return true;

        }
        throw new UserException(environment.getProperty("user.invalidcredentials"), HttpStatus.FORBIDDEN.value());
    }

    @Override
    public boolean verify(String token) {
        long id = JwtGenerator.decodeJWT(token);
        UserModel userInfo = userRepository.findByUserId(id);
        if (id > 0 && userInfo != null) {
            if (!userInfo.isVerified()) {
                userInfo.setVerified(true);
                userInfo.setUpdatedAt(LocalDateTime.now());
                userRepository.save(userInfo);
                return true;
            }
            throw new UserVerificationException(HttpStatus.CREATED.value(),
                    environment.getProperty("user.already.verified"));
        }
        return false;
    }

    @Override
    public UserDetailsResponse forgetPassword(ForgotPasswordDto userMail) {
        UserModel isIdAvailable = userRepository.findByEmailId(userMail.getEmailId());
        if (isIdAvailable != null && isIdAvailable.isVerified()) {
            String token = JwtGenerator.createJWT(isIdAvailable.getUserId(), REGISTRATION_EXP);
            String response = RESETPASSWORD_URL + token;
            if (rabbitMQSender.send(new EmailObject(isIdAvailable.getEmailId(), "ResetPassword Link...", response, "Reset Password Link")))
                return new UserDetailsResponse(HttpStatus.OK.value(), "ResetPassword link Successfully", token);
        }
        return new UserDetailsResponse(HttpStatus.OK.value(), "Eamil ending failed");
    }

    @Override
    public boolean resetPassword(ResetPasswordDto resetPassword, String token) throws UserNotFoundException {
        if (resetPassword.getNewPassword().equals(resetPassword.getConfirmPassword())) {
            long id = JwtGenerator.decodeJWT(token);
            UserModel isIdAvailable = userRepository.findByUserId(id);
            if (isIdAvailable != null) {
                isIdAvailable.setPassword(bCryptPasswordEncoder.encode((resetPassword.getNewPassword())));
                userRepository.save(isIdAvailable);
                redis.putMap(redisKey, resetPassword.getNewPassword(), token);
                return true;
            }
            throw new UserNotFoundException(environment.getProperty("user.not.exist"));
        }
        return false;
    }

    @Override
    public Response login(LoginDto loginDTO) throws UserException {
        UserModel userCheck = userRepository.findByEmailId(loginDTO.getEmailId());

        if (userCheck == null) {
            throw new UserException(environment.getProperty("user.not.found"), HttpStatus.NOT_FOUND.value());
        }
        if (!userCheck.isVerified()) {
            throw new UserException(environment.getProperty("unverified.user"), HttpStatus.BAD_REQUEST.value());
        }
      /*  if(!loginDTO.getRoleType().equals(userCheck.getRoleType())){
            System.out.println(loginDTO.getRoleType()+" "+userCheck.getRoleType());
            throw new UserException(environment.getProperty("user.invalid.credential"),HttpStatus.BAD_REQUEST.value());
        }*/
        if (bCryptPasswordEncoder.matches(loginDTO.getPassword(), userCheck.getPassword())) {
            String token = JwtGenerator.createJWT(userCheck.getUserId(), REGISTRATION_EXP);
            redis.putMap(redisKey, userCheck.getEmailId(), userCheck.getPassword());
            userCheck.setUserStatus(true);
            userRepository.save(userCheck);
            // LoginResponse loginResponse = new LoginResponse(token,userCheck.getFullName(),userCheck.getRoleType());
            return new Response(userCheck.getFullName(), HttpStatus.OK.value(), userCheck.getRoleType(), token);
        }

        throw new UserException(environment.getProperty("user.invalid.credential"), HttpStatus.FORBIDDEN.value());
    }

    /*  @Override
      public Response addToCart(Long bookId) throws BookException {
          BookModel bookModel = bookRepository.findById(bookId)
                  .orElseThrow(() -> new BookException(environment.getProperty("book.not.exist"),HttpStatus.NOT_FOUND));

          if (bookModel.isVerfied()) {
              CartModel cartModel = new CartModel();
              cartModel.setBook_id(bookId);
              cartModel.setName(bookModel.getBookName());
              cartModel.setAuthor(bookModel.getAuthorName());
              cartModel.setTotalPrice(bookModel.getPrice());
              cartModel.setImgUrl(bookModel.getBookImgUrl());
              cartModel.setQuantity(1);
              cartModel.setMaxQuantity(bookModel.getQuantity());
              cartRepository.save(cartModel);
              int size = cartRepository.findAll().size();
              return new Response(size, environment.getProperty("book.added.to.cart.successfully"), HttpStatus.OK.value(), cartModel);
          }
          throw new BookException(environment.getProperty("book.unverified"), HttpStatus.OK);

      }*/
    @Override
    public Response addToCart(CartDto cartDto, Long bookId, String token) {
        long id = JwtGenerator.decodeJWT(token);
        Optional<CartModel> book = cartRepository.findByBookIdAndUserId(bookId, id);
        if (book.isPresent()) {
            if (cartDto == null) {
                cartRepository.delete(book.get());
            }
            BeanUtils.copyProperties(cartDto, book.get());
        } else {
            CartModel cartModel = new CartModel();
            BeanUtils.copyProperties(cartDto, cartModel);
            cartModel.setUserId(id);
            cartRepository.save(cartModel);
        }
        return new Response(environment.getProperty("book.added.to.cart.successfully"), HttpStatus.OK.value(), id);

    }

    @Override
    public Response addMoreItems(Long bookId) throws BookException {

        CartModel cartModel = cartRepository.findByBookId(bookId)
                .orElseThrow(() -> new BookException(environment.getProperty("book.not.added"), HttpStatus.NOT_FOUND.value()));

        int quantity = cartModel.getQuantity();
        cartModel.setTotalPrice(cartModel.getTotalPrice() * (quantity + 1) / quantity);
        quantity++;
        cartModel.setQuantity(quantity);
        cartRepository.save(cartModel);
        return new Response(environment.getProperty("book.added.to.cart.successfully"), HttpStatus.OK.value(), cartModel);
    }

    @Override
    public Response addItems(Long bookId, int quantity) throws BookException {
        CartModel cartModel = cartRepository.findByBookId(bookId)
                .orElseThrow(() -> new BookException(environment.getProperty("book.not.added"), HttpStatus.NOT_FOUND.value()));
        double price = cartModel.getTotalPrice() / cartModel.getQuantity();
        cartModel.setTotalPrice(price * quantity);
        cartModel.setQuantity(quantity);
        cartRepository.save(cartModel);
        return new Response(environment.getProperty("book.added.to.cart.successfully"), HttpStatus.OK.value(), cartModel);
    }


    @Override
    public Response removeItem(Long bookId) throws BookException {

        CartModel cartModel = cartRepository.findByBookId(bookId)
                .orElseThrow(() -> new BookException(environment.getProperty("book.not.added"), HttpStatus.NOT_FOUND.value()));
        int quantity = cartModel.getQuantity();
//        if (quantity == 0) {
//            cartRepository.deleteById(cartModel.getId());
//            return new Response(HttpStatus.OK.value(), environment.getProperty("items.removed.success"));
//        }
        cartModel.setTotalPrice(cartModel.getTotalPrice() * (quantity - 1) / quantity);
        quantity--;
        cartModel.setQuantity(quantity);
        cartRepository.save(cartModel);
        return new Response(environment.getProperty("one.quantity.removed.success"), HttpStatus.OK.value(), cartModel);
    }

    @Override
    public Response removeByBookId(Long bookId, String token) throws BookException {
        long id = JwtGenerator.decodeJWT(token);
        CartModel cartModel = cartRepository.findByBookIdAndUserId(bookId, id)
                .orElseThrow(() -> new BookException(environment.getProperty("book.not.added"), HttpStatus.NOT_FOUND.value()));
        cartRepository.deleteById(cartModel.getId());
        return new Response(HttpStatus.OK.value(), environment.getProperty("quantity.removed.success"));
    }

    @Override
    public Response removeAll(String token) {
        long id = JwtGenerator.decodeJWT(token);
        List<CartModel> cartList = cartRepository.findByUserId(id);
        for (CartModel book : cartList) {
            BookModel bookModel = bookRepository.findByBookId(book.getBookId());
            int netQuantity = bookModel.getQuantity() - book.getQuantity();
            bookModel.setQuantity(netQuantity);
            bookRepository.save(bookModel);
            cartRepository.deleteById(book.getId());
        }
        return new Response(HttpStatus.OK.value(), environment.getProperty("quantity.removed.success"));
    }

    /*  @Override
      public List<CartModel> getAllItemFromCart() throws BookException {
          List<CartModel> items = cartRepository.findAll();
          if (items.isEmpty())
              throw new BookException(environment.getProperty("cart.empty"), HttpStatus.NOT_FOUND);
          return items;
      }*/
    @Override
    public List<CartModel> getAllItemFromCart(String token) throws BookException {
        long id = JwtGenerator.decodeJWT(token);
        System.out.println("id" + id);
        List<CartModel> items = cartRepository.findByUserId(id);
        System.out.println("items" + items);
       /* if (items.isEmpty())
            throw new BookException(environment.getProperty("cart.empty"), HttpStatus.NOT_FOUND);*/
        return items;
    }


    @Override
    public List<BookModel> sortBookByAsc() {
        return bookRepository.sortBookAsc();
    }

    @Override
    public List<BookModel> sortBookByDesc() {
        return bookRepository.sortBookDesc();
    }

    @Override
    public List<BookModel> getAllBooks() throws UserException {
        List<BookModel> book = bookRepository.getAllBooks();
        return book;
    }

    @Override
    public String uploadFile(MultipartFile file, String token) {
        String url = amazonS3ClientService.uploadFile(file);
        long id = JwtGenerator.decodeJWT(token);
        UserModel user = userRepository.findById(id).get();
        user.setProfileUrl(url);
        userRepository.save(user);
        if (RoleType.SELLER == user.getRoleType()) {
            SellerModel seller = sellerRepository.getSeller(id).get();
            seller.setImgUrl(url);
            sellerRepository.save(seller);
        }
        return url;
    }


    /*  @Override
      public List<BookModel> getAllBooks() throws UserException
      {
          List<BookModel> booklist=bookRepository.getAllBooks();
          return booklist;
      }*/
    @Override
    public List<BookModel> getAllVerifiedBooks() throws UserException {
        List<BookModel> booklist = bookRepository.getAllVerifiedBooks();
        return booklist;
    }

    @Override
    public BookModel getBookDetails(Long bookid) throws UserException {
        BookModel bookdetail = bookRepository.getBookDetail(bookid);
        return bookdetail;
    }


//	@Override
//	public BookModel getBookDetails(Long bookid) throws UserException
//	{
//	  BookModel bookdetail=bookRepository.getBookDetail(bookid);
//	  if(bookdetail==null)
//	  {
//		  throw new UserException("Book is not available",null);
//	  }
//		return bookdetail;
//	}
//
//    @Override
//    public Response addToCart(String token, Long bookId) throws BookException, UserNotFoundException {
//        BookModel bookModel = bookRepository.findById(bookId)
//                .orElseThrow(() -> new UserNotFoundException(environment.getProperty("book.not.exist")));
//
//        Long userId = JwtGenerator.decodeJWT(token);
//        if (bookModel.isVerfied()) {
//            CartModel cartModel = new CartModel();
//            cartModel.setBook_id(bookId);
//            cartModel.setQuantity(1L);
//            cartModel.setId(userId);
//            cartRepository.save(cartModel);
//        }
//        throw new BookException("Book is not verified by Admin ", HttpStatus.OK);
//
//    }

    /************************ user details ****************************/
    @Override
    public UserAddressDetailsResponse getUserDetails(String token) {
        long userId = JwtGenerator.decodeJWT(token);
        UserModel user = userRepository.findByUserId(userId);
        List<UserDetailsDTO> allDetailsByUser = user.getListOfUserDetails().stream().map(this::mapData).collect(toList());
        if (allDetailsByUser.isEmpty())
            return new UserAddressDetailsResponse(HttpStatus.OK.value(), environment.getProperty("user.details.nonAvailable"));
        return new UserAddressDetailsResponse(HttpStatus.OK.value(), environment.getProperty("user.details.available"), allDetailsByUser);
    }

    private UserDetailsDTO mapData(UserDetails details) {
        UserDetailsDTO userDto = new UserDetailsDTO();
        BeanUtils.copyProperties(details, userDto);
        return userDto;
    }

    @Override
    public Response addUserDetails(UserDetailsDTO userDetail, String locationType, long userId) {
        UserDetails userDetailsDAO = new UserDetails();
        BeanUtils.copyProperties(userDetail, userDetailsDAO);
        UserModel user = userRepository.findByUserId(userId);
        userDetailsDAO.setUserId(userId);
        userDetailsDAO.setLocationType(locationType);
        user.addUserDetails(userDetailsDAO);
        userRepository.save(user);
        userDetailsDAO.setUser(user);
        userDetailsRepository.save(userDetailsDAO);
        return new Response(HttpStatus.OK.value(), environment.getProperty("user.details.added"));
    }

    @Override
    public Response deleteUserDetails(UserDetailsDTO userDetail, long userId) {
        UserModel userModel = userRepository.findByUserId(userId);
        UserDetails userDetailsDAO = userDetailsRepository.findByAddressAndUserId(userDetail.getAddress(), userId);
        userModel.removeUserDetails(userDetailsDAO);
        userDetailsRepository.delete(userDetailsDAO);
        userRepository.save(userModel);
        return new Response(HttpStatus.OK.value(), environment.getProperty("user.details.deleted"));
    }

    @Override
    public Long getIdFromToken(String token) {
        Long id = jwtop.decodeJWT(token);
        return id;
    }

    @Override
    public Optional<BookModel> searchBookByName(String bookName) {
        Optional<BookModel> book = bookRepository.searchBookByName(bookName);
        return book;
    }

    @Override
    public Optional<BookModel> searchBookByAuthor(String authorName) {
        Optional<BookModel> book = bookRepository.searchBookByAuthor(authorName);
        return book;
    }

    @Override
    public long getOrderId() {
        Date date = new Date();
        //getTime() returns current time in milliseconds
        long time = date.getTime();
        //Passed the milliseconds to constructor of Timestamp class 
        Timestamp ts = new Timestamp(time);
        return time;
    }

    @Override
    public Response orderPlaced(String token) throws BookException {
        long id = JwtGenerator.decodeJWT(token);
        UserModel userInfo = userRepository.findByUserId(id);
        List<CartModel> allItemFromCart = getAllItemFromCart(token);
        long orderId = getOrderId();
        String bookName = "";
        String price = "\n";
        double totalPrice = 0;
        String quantity;
        for (CartModel cartModel : allItemFromCart) {
            BookModel bookModel = bookRepository.findByBookId(cartModel.getBookId());
            bookName = bookName + bookModel.getBookName() + " (Rs." + price + bookModel.getPrice() + ")\n";
            totalPrice = totalPrice + cartModel.getTotalPrice();
            bookModel.setQuantity(bookModel.getQuantity() - cartModel.getQuantity());
            bookRepository.save(bookModel);
            OrderPlaced order = new OrderPlaced();
            BeanUtils.copyProperties(cartModel, order);
            order.setOrderId(orderId);
            order.setPrice(cartModel.getTotalPrice());
            order.setQuantity(cartModel.getQuantity());
            orderRepository.save(order);
        }
        if (userInfo != null) {
            String response =
                    "==================\n" +
                            "ONLINE BOOK STORE \n" +
                            "==================\n\n" +
                            "Hello " + userInfo.getFullName() + ",\n\n" +
                            "Your order has been placed successfully.\n" +
                            "----------------------------------------------------------------\n" +
                            "Your OrderId is " + orderId + "\n" +
                            "*Book* Name : " + bookName + "\n" +
                            "Total Items : " + allItemFromCart.size() + "\n" +
                            "----------------------------------------------------------------\n" +
                            "Total Price : Rs." + totalPrice + "\n" +
                            "\n\n" +
                            "Thank you for Shopping with us.\n" +
                            "Have a great Experience with us !!" +
                            "\n\n" +
                            "Thank you,\n" +
                            "Online Book Store Team, Bangalore\n" +
                            "Contact us\n" +
                            "mob. : +91-9771971429\n" +
                            "email : admin@onlinebookstore.com\n";
            if (rabbitMQSender.send(new EmailObject(userInfo.getEmailId(), "Order Placed Successfully..", response, "Order Placed"))) {
                return new Response("Order Successfull", HttpStatus.OK.value(), orderId);
            }
        }
        throw new BookException(environment.getProperty("book.unverified"), HttpStatus.OK.value());
    }

    @Override
    public Response addToWishList(Long bookId, String token) {
        long id = JwtGenerator.decodeJWT(token);
        if (!wish.existsByBookIdAndUserId(bookId, id)) {
            WishListModel wishListModel = new WishListModel();
            BookModel bookModel = bookRepository.findByBookId(bookId);
            BeanUtils.copyProperties(bookModel, wishListModel);
            wishListModel.setUserId(JwtGenerator.decodeJWT(token));
            wish.save(wishListModel);
            List<WishListModel> cart = wish.findAllByUserId(id);
            return new Response("Book added to WishList", HttpStatus.OK.value(), cart);
        }
        return new Response(HttpStatus.OK.value(), "Book already in WishList");
    }

    @Override
    public Response deleteFromWishlist(Long bookId, String token) {
        long id = JwtGenerator.decodeJWT(token);
        WishListModel byBookIdAndUserId = wish.findByBookIdAndUserId(bookId, id);
        wish.delete(byBookIdAndUserId);
        List<WishListModel> cart = wish.findAllByUserId(id);
        return new Response("Book deleted from WishList", HttpStatus.OK.value(), cart);
    }

    @Override
    public Response addFromWishlistToCart(Long bookId, String token) {
        long id = JwtGenerator.decodeJWT(token);
        if (!cartRepository.existsByBookIdAndUserId(bookId, id)) {
            CartModel cartModel = new CartModel();
            BookModel bookModel = bookRepository.findByBookId(bookId);
            BeanUtils.copyProperties(bookModel, cartModel);
            cartModel.setName(bookModel.getBookName());
            cartModel.setAuthor(bookModel.getAuthorName());
            cartModel.setImgUrl(bookModel.getBookImgUrl());
            cartModel.setTotalPrice(bookModel.getPrice());
            cartModel.setUserId(id);
            cartModel.setQuantity(1);
            cartModel.setMaxQuantity(bookModel.getQuantity());
            WishListModel byBookIdAndUserId = wish.findByBookIdAndUserId(bookId, id);
            wish.delete(byBookIdAndUserId);
            cartRepository.save(cartModel);
            return new Response(HttpStatus.OK.value(), "Book added to Cart from wishlist");
        }
        return new Response(HttpStatus.OK.value(), "Book Already in Cart");
    }

    @Override
    public Response getAllItemFromWishList(String token) {
        long id = JwtGenerator.decodeJWT(token);
        List<WishListModel> wishListModels = wish.findAllByUserId(id);
        return new Response("Book added to WishList", HttpStatus.OK.value(), wishListModels);
    }
}