package me.cprox.practice.util.bootstrap;

import me.cprox.practice.Practice;
import lombok.Getter;

@Getter
public class Bootstrapped {

    protected final Practice Practice;

    public Bootstrapped(Practice Practice) {
        this.Practice = Practice;
    }

}
