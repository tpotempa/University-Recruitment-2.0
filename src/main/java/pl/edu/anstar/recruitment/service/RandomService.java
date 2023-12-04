package pl.edu.anstar.recruitment.service;

import java.util.Random;

public class RandomService {

    public RandomService() {
    }

    public String generateRandom(int length) {
        var stringBuilder = new StringBuilder();
        var random = new Random();

        for (int i = 0; i < length; ++i) {
            boolean appendChar = random.nextBoolean();

            if (appendChar) {
                stringBuilder.append((char) ('A' + random.nextInt(26)));
            } else {
                stringBuilder.append(random.nextInt(9));
            }
        }

        return stringBuilder.toString();
    }
}