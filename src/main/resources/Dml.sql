INSERT into email_template(`template`, `subject`, `body`, `informer`, `cc`, `created`, `status`, `name`) values ('<!DOCTYPE html><html><head><style> .titleText {color: red;font-size: xx-large;}</style><meta name="viewport" content="width=device-width, initial-scale=1"><meta http-equiv="Content-Type" content="text/html; charset=utf-8"/><title>Welcome Mail</title></head><body width="100%" align="center" style="margin:0; padding: 0; mso-line-height-rule: exactly;"><table align="center" border="0" cellpadding="0" cellspacing="0" width="600" style="border-collapse: collapse;"><tr><td align="center" style="border:1px solid #ccc; padding-top: 21px; padding-bottom: 21px; padding-left: 20px; padding-right: 20px;"><table bgcolor="white" align="center" border="0" cellpadding="0" cellspacing="0" width="100%"><tr><td align="center" style="padding-top: 10px;background-color: #a03037; border-radius: 2px;padding-top: 14px; padding-bottom: 14px; padding-left: 73px; padding-right: 73px; "><div class="titleText" fxLayoutGap="3%"><!--                            <img style="color: red"--><!--                                 src="https://online-book.s3.ap-south-1.amazonaws.com/education.svg"/>--><svg xmlns="http://www.w3.org/2000/svg" width="31.039" height="23.713" viewBox="0 0 31.039 23.713"><defs><style>.a {fill: #fff;}</style></defs><g transform="translate(0 -35.048)"><g transform="translate(0 35.048)"><g transform="translate(0 0)"><path class="a" d="M35.941,35.049h0a1.1,1.1,0,0,0-.778.322,1.106,1.106,0,0,0-.327.788V52.815a1.114,1.114,0,0,0,1.112,1.11c2.585.006,6.917.545,9.9,3.672V40.167A1.064,1.064,0,0,0,45.7,39.6C43.245,35.655,38.532,35.055,35.941,35.049Z" transform="translate(-31.193 -35.049)"/><path class="a" d="M167.768,52.814V36.159a1.106,1.106,0,0,0-.327-.788,1.1,1.1,0,0,0-.778-.322h0c-2.591.006-7.3.606-9.757,4.556a1.064,1.064,0,0,0-.153.562V57.6c2.988-3.127,7.32-3.666,9.9-3.672A1.114,1.114,0,0,0,167.768,52.814Z" transform="translate(-140.369 -35.048)"/><path class="a" d="M183.489,71.8h-.805V85.726a2.842,2.842,0,0,1-2.832,2.835c-2.193.005-5.809.434-8.369,2.858a26.738,26.738,0,0,1,11.758.227,1.111,1.111,0,0,0,1.359-1.082V72.912A1.112,1.112,0,0,0,183.489,71.8Z" transform="translate(-153.561 -67.96)"/><path class="a" d="M1.916,85.726V71.8H1.111A1.112,1.112,0,0,0,0,72.912V90.563a1.111,1.111,0,0,0,1.359,1.082,26.737,26.737,0,0,1,11.758-.227c-2.561-2.424-6.176-2.852-8.369-2.857A2.842,2.842,0,0,1,1.916,85.726Z" transform="translate(0 -67.96)"/></g></g></g></svg><a href="${url} adminDashboard/seller-list" style="color: white ; text-decoration: none;">BookStore</a></div></td></tr><tr><td style="font-size: 14px;color: #4a4a4a; font-family: Arial;  padding-top: 38px;"> You have registred your email id : <span style="font-weight: 600">${email}</span> with us. click on button to verify your account<!--                        <span style="font-weight: 600">click on button to verify your account </span></td>--></tr><tr><td align="center" style="padding-top: 84px; padding-right: 0px;padding-bottom: 20px; padding-left: 0px;"><table bgcolor="#0b9446" align="center" border="0" cellpadding="0" cellspacing="0" style="border-radius: 2px;"><tbody><tr><td align="center" style="background-color: #a03037; border-radius: 2px;padding-top: 14px; padding-bottom: 14px; padding-left: 73px; padding-right: 73px; "><a href="${linkurl}" style="color: white ; text-decoration: none;"><!--                                       style="text-decoration: none;font-size: 17px;color: #ffffff;font-family: Arial; font-weight: 600;height: 48px;">-->Click here to verify </a></td></tr></tbody></table></td></tr><tr><td style="width: 100%; border-top: 1px solid #ccc;"></td></tr><tr><td align="center" style="width: 100%;font-size: 12px; color: #4a4a4a;font-family: Arial; padding-top: 19px;padding-right: 6px;padding-bottom: 0px;padding-left: 6px;">The content of this email is confidential and intended for the recipient specified in message only. It is strictly forbidden to share any part of this message with any third party, without a written consent of the sender. If you received this message by mistake, please reply to this message and follow with its deletion, so that we can ensure such a mistake does not occur in the future.</td></tr></table></td></tr></table></body></html>', 'Registration Link...', null , null, null, now(), 'ACTIVE', 'REGISTRATION_MAIL');