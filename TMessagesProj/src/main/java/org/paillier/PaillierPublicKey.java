package org.paillier;

import java.math.BigInteger;
import java.util.Random;

/**
 * Created by gzq on 16-1-11.
 */
public class PaillierPublicKey {
    private final BigInteger n;
    private final BigInteger g;
    private final BigInteger nSquared;
    private final int bits;

    public PaillierPublicKey(BigInteger n, BigInteger g) {
        this.n = n;
        this.g = g;
        nSquared = n.pow(2);
        bits = n.bitLength();
    }

    public BigInteger encrypt(BigInteger m) {
        BigInteger r;
        do {
            r = new BigInteger(bits, new Random());
        } while (r.compareTo(n) >= 0);

        BigInteger result = g.modPow(m, nSquared);
        BigInteger x = r.modPow(n, nSquared);

        result = result.multiply(x);
        result = result.mod(nSquared);

        return result;
    }

    public BigInteger multiple(BigInteger m1, BigInteger m2) {
        return m1.modPow(m2, nSquared);
    }
}
