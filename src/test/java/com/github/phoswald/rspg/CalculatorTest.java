package com.github.phoswald.rspg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import parsers.Calculator;

class CalculatorTest {

    private final Calculator testee = new Calculator(new CalculatorCallback());

    @Test
    void parseExpression_symbol() {
        assertEquals(Math.PI, testee.parseExpression("pi"));
        assertEquals(Math.E, testee.parseExpression("e"));
        assertNull(testee.parseExpression("x"));
        assertNull(testee.parseExpression(""));
    }

    @Test
    void parseExpression_number() {
        assertEquals(1234.0, testee.parseExpression("1234"));
        assertEquals(1234.0, testee.parseExpression("001234"));
    }

    @Test
    void parseExpression_add() {
        assertEquals(12 + 34, testee.parseExpression("12+34"));
        assertEquals(34 - 12, testee.parseExpression("34-12"));

        assertEquals(12 + 34 + 5, testee.parseExpression("12+34+5"));
        assertEquals(34 - 12 - 5, testee.parseExpression("34-12-5"));
    }

    @Test
    void parseExpression_mul() {
        assertEquals(7 * 3, testee.parseExpression("7*3"));
        assertEquals(21 / 3, testee.parseExpression("21/3"));

        assertEquals(7 * 3 * 4, testee.parseExpression("7*3*4"));
        assertEquals(84 / 3 / 4, testee.parseExpression("84/3/4"));
    }

    @Test
    void parseExpression_predcedence() {
        assertEquals(2 * 2 + 5 * 5, testee.parseExpression("2*2+5*5"));
        assertEquals(2 * 2 - 5 * 5, testee.parseExpression("2*2-5*5"));
    }

    @Test
    void parseExpression_braces() {
        assertEquals(12 + 34, testee.parseExpression("(12+34)"));
        assertEquals(12 + 34, testee.parseExpression("12+(34)"));
        assertEquals(12 + 34, testee.parseExpression("((12)+(34))"));
        assertEquals(2 * 2 - 5 * 5, testee.parseExpression("(2*2)-(5*5)"));
        assertEquals(2 * (2 - 5) * 5, testee.parseExpression("2*(2-5)*5"));
    }

    private class CalculatorCallback implements Calculator.Callback {

        @Override
        public Double add(Double output, Double element1) {
            return Double.valueOf(output.doubleValue() + element1.doubleValue());
        }

        @Override
        public Double sub(Double output, Double element1) {
            return Double.valueOf(output.doubleValue() - element1.doubleValue());
        }

        @Override
        public Double mul(Double output, Double element1) {
            return Double.valueOf(output.doubleValue() * element1.doubleValue());
        }

        @Override
        public Double div(Double output, Double element1) {
            return Double.valueOf(output.doubleValue() / element1.doubleValue());
        }

        @Override
        public Double getPi() {
            return Double.valueOf(Math.PI);
        }

        @Override
        public Double getE() {
            return Double.valueOf(Math.E);
        }

        @Override
        public Double createNumber(Integer element1) {
            return Double.valueOf(element1.doubleValue());
        }

        @Override
        public Integer handleDigit(Integer output, String token1) {
            return Integer.valueOf((output == null ? 0 : output.intValue()) * 10 + (token1.charAt(0) - '0'));
        }
    }
}
