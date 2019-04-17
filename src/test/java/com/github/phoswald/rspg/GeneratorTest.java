package com.github.phoswald.rspg;

import static com.github.phoswald.rspg.Alternative.alternative;
import static com.github.phoswald.rspg.Symbol.symbol;
import static com.github.phoswald.rspg.Token.set;
import static com.github.phoswald.rspg.Token.token;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class GeneratorTest {

    @Test
    void generate_calculator() throws IOException {
        // Arrange
        List<Rule> rules = new ArrayList<>();
        rules.add(Rule.builder() //
                .name("Expression") //
                .javaType("Double") //
                .export(true) //
                .alternative(alternative(symbol("ExprAdd").linked())) //
                .build());

        rules.add(Rule.builder() //
                .name("ExprAdd") //
                .javaType("Double") //
                .alternative(alternative(symbol("ExprMul").linked(), symbol("OpAdd").linked())) //
                .build());
        rules.add(Rule.builder() //
                .name("OpAdd") //
                .javaType("Double") //
                .alternative(alternative(token("+"), symbol("ExprMul").callbackLinked("add"), symbol("OpAdd").linked())) //
                .alternative(alternative(token("-"), symbol("ExprMul").callbackLinked("sub"), symbol("OpAdd").linked())) //
                .alternative(alternative()) //
                .build());

        rules.add(Rule.builder() //
                .name("ExprMul") //
                .javaType("Double") //
                .alternative(alternative(symbol("Brace").linked(), symbol("OpMul").linked())) //
                .build());
        rules.add(Rule.builder() //
                .name("OpMul") //
                .javaType("Double") //
                .alternative(alternative(token("*"), symbol("Brace").callbackLinked("mul"), symbol("OpMul").linked())) //
                .alternative(alternative(token("/"), symbol("Brace").callbackLinked("div"), symbol("OpMul").linked())) //
                .alternative(alternative()) //
                .build());

        rules.add(Rule.builder() //
                .name("Brace") //
                .javaType("Double") //
                .alternative(alternative(token("("), symbol("Expression").linked(), token(")"))) //
                .alternative(alternative(symbol("Value").linked())) //
                .build());

        rules.add(Rule.builder() //
                .name("Value") //
                .javaType("Double") //
                .alternative(alternative(token("pi").callback("getPi"))) //
                .alternative(alternative(token("e").callback("getE"))) //
                .alternative(alternative(symbol("Number").callback("createNumber"))) //
                .build());
        rules.add(Rule.builder() //
                .name("Number") //
                .javaType("Integer") //
                .alternative(alternative(symbol("Digit").linked(), symbol("Digits").linked())) //
                .build());
        rules.add(Rule.builder() //
                .name("Digit") //
                .javaType("Integer") //
                .alternative(alternative(set("0123456789").pass().callbackLinked("handleDigit"))) //
                .build());
        rules.add(Rule.builder() //
                .name("Digits") //
                .javaType("Integer") //
                .alternative(alternative(symbol("Digit").linked(), symbol("Digits").linked())) //
                .alternative(alternative()) //
                .build());
        Grammar grammar = Grammar.builder() //
                .name("Calculator") //
                .javaType("parsers.Calculator") //
                .rules(rules) //
                .build();

        // Act
        new Generator(grammar).generate(Paths.get("target/generated-parsers"));

        // Assert
        assertEqualTextFiles( //
                Paths.get("src/test/java/parsers/Calculator.java"), //
                Paths.get("target/generated-parsers/parsers/Calculator.java"));
    }

    private void assertEqualTextFiles(Path expectedFile, Path actualFile) throws IOException {
        String expectedText = String.join("\n", Files.readAllLines(expectedFile));
        String actualText = String.join("\n", Files.readAllLines(actualFile));
        assertTrue(expectedText.equals(actualText), () -> "Files differ: " + expectedFile + ", " + actualFile);
    }
}
