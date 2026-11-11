package com.calculator.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.DecimalFormat;

public class MainActivity extends Activity {

    LinearLayout root;
    TextView display, history;
    String expression = "";
    boolean calculated = false;

    int bg = Color.rgb(7, 12, 25);
    int blue = Color.rgb(20, 130, 255);
    int white = Color.WHITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
    }

    void buildUI() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 25, 18, 18);
        root.setBackgroundColor(bg);

        TextView title = new TextView(this);
        title.setText("CALCULATOR");
        title.setTextColor(blue);
        title.setTextSize(25);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, 65));

        history = new TextView(this);
        history.setText("Scientific Calculator");
        history.setTextColor(Color.GRAY);
        history.setTextSize(14);
        history.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        root.addView(history, new LinearLayout.LayoutParams(-1, 65));

        display = new TextView(this);
        display.setText("0");
        display.setTextColor(white);
        display.setTextSize(42);
        display.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        display.setSingleLine(true);
        root.addView(display, new LinearLayout.LayoutParams(-1, 100));

        String[][] keys = {
            {"AC", "DEL", "%", "÷"},
            {"7", "8", "9", "×"},
            {"4", "5", "6", "−"},
            {"1", "2", "3", "+"},
            {"√", "0", ".", "="}
        };

        for (String[] row : keys) {
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);

            for (String key : row) {
                Button b = new Button(this);
                b.setText(key);
                b.setTextSize(21);
                b.setAllCaps(false);
                b.setTextColor(white);

                int color = key.equals("=") || 
                    "+−×÷".contains(key) ? blue :
                    key.equals("AC") || key.equals("DEL") ?
                    Color.rgb(45, 55, 75) :
                    Color.rgb(22, 32, 50);

                b.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(color)
                );

                LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0, 75, 1);
                lp.setMargins(4, 5, 4, 5);
                line.addView(b, lp);

                b.setOnClickListener(v -> press(key));
            }

            root.addView(line);
        }

        TextView footer = new TextView(this);
        footer.setText("CALCULATOR • PREMIUM");
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, new LinearLayout.LayoutParams(-1, 55));

        setContentView(root);
    }

    void press(String key) {
        if (key.equals("AC")) {
            expression = "";
            display.setText("0");
            history.setText("Scientific Calculator");
            calculated = false;
            return;
        }

        if (key.equals("DEL")) {
            if (!expression.isEmpty())
                expression = expression.substring(0, expression.length()-1);
            update();
            return;
        }

        if (key.equals("=")) {
            try {
                double result = new CalculatorParser(expression).parse();
                history.setText(expression + " =");
                expression = new DecimalFormat("0.##########").format(result);
                display.setText(expression);
                calculated = true;
            } catch (Exception e) {
                display.setText("Error");
                expression = "";
            }
            return;
        }

        if (key.equals("√")) {
            try {
                double value = new CalculatorParser(expression).parse();
                if (value < 0) throw new Exception();
                history.setText("√(" + expression + ")");
                expression = String.valueOf(Math.sqrt(value));
                display.setText(expression);
                calculated = true;
            } catch (Exception e) {
                display.setText("Error");
                expression = "";
            }
            return;
        }

        if (key.equals("%")) {
            expression += "/100";
        } else {
            if (calculated && "0123456789.".contains(key)) {
                expression = "";
            }
            expression += key;
        }

        calculated = false;
        update();
    }

    void update() {
        display.setText(expression.isEmpty() ? "0" : expression);
    }

    class CalculatorParser {
        String s;
        int pos = -1, ch;

        CalculatorParser(String input) {
            s = input.replace("×", "*")
                     .replace("÷", "/")
                     .replace("−", "-")
                     .replace(" ", "");
        }

        void next() {
            ch = (++pos < s.length()) ? s.charAt(pos) : -1;
        }

        boolean eat(int c) {
            while (ch == ' ') next();
            if (ch == c) {
                next();
                return true;
            }
            return false;
        }

        double parse() {
            next();
            double x = expression();
            if (pos < s.length()) throw new RuntimeException();
            if (!Double.isFinite(x)) throw new RuntimeException();
            return x;
        }

        double expression() {
            double x = term();
            while (true) {
                if (eat('+')) x += term();
                else if (eat('-')) x -= term();
                else return x;
            }
        }

        double term() {
            double x = factor();
            while (true) {
                if (eat('*')) x *= factor();
                else if (eat('/')) {
                    double d = factor();
                    if (d == 0) throw new RuntimeException();
                    x /= d;
                }
                else return x;
            }
        }

        double factor() {
            if (eat('+')) return factor();
            if (eat('-')) return -factor();

            double x;
            int start = pos;

            if (eat('(')) {
                x = expression();
                if (!eat(')')) throw new RuntimeException();
            } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                while ((ch >= '0' && ch <= '9') || ch == '.') next();
                x = Double.parseDouble(s.substring(start, pos));
            } else {
                throw new RuntimeException();
            }

            return x;
        }
    }
}
