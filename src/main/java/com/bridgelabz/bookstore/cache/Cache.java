package com.bridgelabz.bookstore.cache;

public abstract class Cache {

    protected abstract void constructCache();

    public abstract void load();

    public abstract void reload();
}
