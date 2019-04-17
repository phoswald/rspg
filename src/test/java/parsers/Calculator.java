package parsers;

public class Calculator {

    private final Callback callback;
    private String input;
    private int offset;

    public Calculator(Callback callback) {
        this.callback = callback;
    }

    public Double parseExpression(String input) {
        this.input = input;
        this.offset = 0;
        Ref<Double> output = new Ref<>();
        if (parseExpression(output) && this.offset == input.length()) {
            return output.value;
        } else {
            return null;
        }
    }

    private boolean parseExpression(Ref<Double> output) {
        int offset = this.offset;
        {
            if (parseExprAdd(output)) {
                return true;
            }
        }
        this.offset = offset;
        return false;
    }

    private boolean parseExprAdd(Ref<Double> output) {
        int offset = this.offset;
        {
            if (parseExprMul(output)) {
                if (parseOpAdd(output)) {
                    return true;
                }
            }
        }
        this.offset = offset;
        return false;
    }

    private boolean parseOpAdd(Ref<Double> output) {
        int offset = this.offset;
        {
            if (matchToken("+")) {
                Ref<Double> element1 = new Ref<>();
                if (parseExprMul(element1)) {
                    output.value = callback.add(output.value, element1.value);
                    if (parseOpAdd(output)) {
                        return true;
                    }
                }
            }
        }
        this.offset = offset;
        {
            if (matchToken("-")) {
                Ref<Double> element1 = new Ref<>();
                if (parseExprMul(element1)) {
                    output.value = callback.sub(output.value, element1.value);
                    if (parseOpAdd(output)) {
                        return true;
                    }
                }
            }
        }
        this.offset = offset;
        {
            return true;
        }
    }

    private boolean parseExprMul(Ref<Double> output) {
        int offset = this.offset;
        {
            if (parseValue(output)) {
                if (parseOpMul(output)) {
                    return true;
                }
            }
        }
        this.offset = offset;
        return false;
    }

    private boolean parseOpMul(Ref<Double> output) {
        int offset = this.offset;
        {
            if (matchToken("*")) {
                Ref<Double> element1 = new Ref<>();
                if (parseValue(element1)) {
                    output.value = callback.mul(output.value, element1.value);
                    if (parseOpMul(output)) {
                        return true;
                    }
                }
            }
        }
        this.offset = offset;
        {
            if (matchToken("/")) {
                Ref<Double> element1 = new Ref<>();
                if (parseValue(element1)) {
                    output.value = callback.div(output.value, element1.value);
                    if (parseOpMul(output)) {
                        return true;
                    }
                }
            }
        }
        this.offset = offset;
        {
            return true;
        }
    }

    private boolean parseValue(Ref<Double> output) {
        int offset = this.offset;
        {
            if (matchToken("pi")) {
                output.value = callback.getPi();
                return true;
            }
        }
        this.offset = offset;
        {
            if (matchToken("e")) {
                output.value = callback.getE();
                return true;
            }
        }
        this.offset = offset;
        {
            Ref<Integer> element1 = new Ref<>();
            if (parseNumber(element1)) {
                output.value = callback.createNumber(element1.value);
                return true;
            }
        }
        this.offset = offset;
        return false;
    }

    private boolean parseNumber(Ref<Integer> output) {
        int offset = this.offset;
        {
            if (parseDigit(output)) {
                if (parseDigits(output)) {
                    return true;
                }
            }
        }
        this.offset = offset;
        return false;
    }

    private boolean parseDigit(Ref<Integer> output) {
        int offset = this.offset;
        {
            int offset1 = this.offset;
            if (matchSet("0123456789")) {
                String token1 = input.substring(offset1, this.offset);
                output.value = callback.handleDigit(output.value, token1);
                return true;
            }
        }
        this.offset = offset;
        return false;
    }

    private boolean parseDigits(Ref<Integer> output) {
        int offset = this.offset;
        {
            if (parseDigit(output)) {
                if (parseDigits(output)) {
                    return true;
                }
            }
        }
        this.offset = offset;
        {
            return true;
        }
    }

    private boolean matchToken(String token) {
        if (this.input.startsWith(token, this.offset)) {
            this.offset += token.length();
            return true;
        } else {
            return false;
        }
    }

    private boolean matchSet(String set) {
        if (offset < input.length() && set.contains(input.substring(offset, offset + 1))) {
            this.offset++;
            return true;
        } else {
            return false;
        }
    }

    private static class Ref<T> {
        T value;
    }

    public static interface Callback {

        public Double add(Double output, Double element1);

        public Double sub(Double output, Double element1);

        public Double mul(Double output, Double element1);

        public Double div(Double output, Double element1);

        public Double getPi();

        public Double getE();

        public Double createNumber(Integer element1);

        public Integer handleDigit(Integer output, String token1);
    }
}
