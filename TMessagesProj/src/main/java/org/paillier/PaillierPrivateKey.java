package org.paillier;

import java.math.BigInteger;

/**
 * Created by gzq on 16-1-13.
 */
public class PaillierPrivateKey {
    private BigInteger lambda;
    private BigInteger mu;
    private BigInteger n;
    private BigInteger nSquare;
    public PaillierPrivateKey(BigInteger lambda, BigInteger mu, BigInteger n) {
        this.lambda = lambda;
        this.mu = mu;
        this.n = n;
        nSquare = n.pow(2);
    }

    public BigInteger decrypt(BigInteger c) {
        BigInteger result0 = c.modPow(lambda, nSquare).subtract(BigInteger.ONE);
        BigInteger result1 = result0.divide(n).multiply(mu);
        return result1.mod(n);
    }
}
