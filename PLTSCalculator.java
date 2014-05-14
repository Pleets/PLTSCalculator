/*
 * PLTS Simple Calculator
 * http://www.pleets.org
 * Copyright 2014, Pleets Apps
 * Free to use under the MIT license.
 * http://www.opensource.org/licenses/mit-license.php
 *
 * Date: 2014-05-14
 *

 * @author  Darío Rivera
 * @email   admin@pleets.org

  Release Notes

  - Partial keyboard support
  - Square root (unary operator) are supported
  - Multiple and associative operations are supported

 */

import java.awt.*;
import java.awt.event.*;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;

class PLTSCalculator extends JFrame implements ActionListener, KeyListener
{
    private Container pane;
    private JScrollPane scroll;

    private JButton
            one, two, three, four, five, six, seven, eight, nine, zero,
            parL, parR, power, div, times, minus, dot, mod, plus, clearE, equal,
            clear, sqrt, ans;

    private JTextArea screen, latestCommand;
    private JLabel notifications;

    private final String unaryRegEx; 
    private final String multipleRegEx;
    private final String associativeRegEx;

    public static void main (String []a)
    {
        PLTSCalculator calc = new PLTSCalculator();
        calc.setBounds(300,300,420,400);
        calc.setResizable(false);
        calc.setVisible(true);
    }    

    PLTSCalculator()
    {
        super("PLTS Simple calculator");
        
        this.unaryRegEx = "^[\\+\\-0-9√][0-9]*([.][0-9]+)?$";
        this.multipleRegEx = "^[\\+\\-0-9√][0-9]*([.][0-9]+)?([\\+\\-*/%^][0-9√]+([.][0-9]*[^.])?)*$";
        
        String inMultipleRegEx = multipleRegEx.substring(1, multipleRegEx.length() - 1);

        String simpleAssociativeRegEx = "(" + inMultipleRegEx +")?([\\(]" + inMultipleRegEx + "[\\)])?";
        this.associativeRegEx = "^(" + simpleAssociativeRegEx + ")*([\\+\\-*/%^]" + simpleAssociativeRegEx + ")*$";

        setGridLayout();

        this.pack();
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);

        // Autofocus
        screen.requestFocus();        
    }    

    private void setGridLayout()
    {
        pane = this.getContentPane();
        pane.setLayout (new GridBagLayout());
        pane.setLocation(100, 100);
        pane.setBackground(new Color(245,245,245));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weighty = 1.0;

        constraints.anchor = GridBagConstraints.EAST;
        constraints.fill = GridBagConstraints.NONE;

        // View the result of the latest command
        latestCommand = new JTextArea("", 1, 10);
        latestCommand.setEditable(false);
        latestCommand.setBackground(new Color(230,230,230));

        pane.add(latestCommand, constraints);

        constraints.anchor = GridBagConstraints.CENTER;

        // Screen
        JTextArea textarea = new JTextArea("", 2, 11);

        textarea.setLineWrap(true);
        textarea.setEditable(false);
        textarea.setWrapStyleWord(true);
        textarea.setBackground(new Color(230,230,230));
        textarea.setFont(new Font("Sans", Font.TRUETYPE_FONT, 35));
 
        scroll = new JScrollPane(textarea);
        screen = textarea;

        constraints.gridx = 0;
        constraints.gridy = 1;

        pane.add(scroll, constraints);
        
        // Adding Controls
        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(4, 6));

        constraints.gridx = 0;
        constraints.gridy = 2;
        pane.add(controls, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 3;

        // Button controls
        seven = new JButton("7");
        controls.add(seven); 
        seven.addActionListener(this);
        eight = new JButton("8");
        controls.add(eight);
        eight.addActionListener(this);
        nine = new JButton("9");
        controls.add(nine);
        nine.addActionListener(this);
        div = new JButton("/");
        controls.add(div);
        div.addActionListener(this);
        clearE = new JButton("CE");
        controls.add(clearE);
        clearE.addActionListener(this);
        clear = new JButton("C");
        controls.add(clear);
        clear.addActionListener(this);
        
        four = new JButton("4");
        controls.add(four);
        four.addActionListener(this);
        five = new JButton("5");
        controls.add(five);
        five.addActionListener(this);
        six = new JButton("6");
        controls.add(six);
        six.addActionListener(this);
        times = new JButton("x");
        controls.add(times);
        parL = new JButton("(");
        controls.add(parL);
        parL.addActionListener(this);   
        parR = new JButton(")");
        controls.add(parR);
        parR.addActionListener(this); 
   
        times.addActionListener(this);
        one = new JButton("1");
        controls.add(one);
        one.addActionListener(this);
        two = new JButton("2");
        controls.add(two);
        two.addActionListener(this);
        three = new JButton("3");
        controls.add(three);
        three.addActionListener(this);
        minus = new JButton("-");
        controls.add(minus);
        power = new JButton("^");
        controls.add(power);
        power.addActionListener(this);
        sqrt = new JButton("√");
        controls.add(sqrt);
        sqrt.addActionListener(this);

        minus.addActionListener(this);
        zero = new JButton("0");
        controls.add(zero);
        zero.addActionListener(this);
        dot = new JButton(".");
        controls.add(dot);
        dot.addActionListener(this);
        mod = new JButton("%");
        controls.add(mod);
        mod.addActionListener(this);
        plus = new JButton("+");
        controls.add(plus);
        plus.addActionListener(this);
        ans = new JButton("ans");
        controls.add(ans);
        ans.addActionListener(this);

        // Bottom controls
        equal = new JButton("=");
        equal.setFont(new Font("Sans", Font.TRUETYPE_FONT, 35));
        controls.add(equal);
        equal.addActionListener(this);

        constraints.gridx = 0;
        constraints.gridy = 4;        
        
        // Notification label
        notifications = new JLabel("© Pleets");
        pane.add(notifications, constraints);

        // Key listener
        textarea.addKeyListener(this);
    }

    private double unaryOperation(String text)
    {
        if (text.contains("√"))
        {
            double value = Double.parseDouble(text.substring(text.indexOf("√") + 1, text.length()));
            return Math.sqrt(value);
        }
        return Double.parseDouble(text);
    }

    private double multipleOperation(String text)
    {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");

        try {
            String number;      // Number to operate

            String[] unaryOperations = text.split("[\\+\\-]");

            for (int i = unaryOperations.length - 1; i >= 0; i--)
            {
                if (unaryOperations[i].contains("√"))
                {
                    number = unaryOperations[i].substring(unaryOperations[i].indexOf("√") + 1);
                    text = text.replace("√"+number,"Math.sqrt("+number+")");
                }
                else if (unaryOperations[i].contains("^"))
                {
                    double exp = Double.parseDouble(unaryOperations[i].substring(unaryOperations[i].indexOf("^") + 1));
                    double base = Double.parseDouble(unaryOperations[i].substring(0,unaryOperations[i].indexOf("^")));
                    text = text.replace(unaryOperations[i].substring(0,unaryOperations[i].indexOf("^"))+"^"+unaryOperations[i].substring(unaryOperations[i].indexOf("^") + 1),"Math.pow("+base+","+exp+")");
                }
            }

            return Double.parseDouble(""+engine.eval(text));

        } catch(ScriptException exception) {
            notifications.setText(exception.getMessage());
        }

        return -1;
    }

    public double associativeOperation(String expression)
    {
        if (expression.matches(associativeRegEx) && expression.contains("("))
        {
            String multiExp = expression.substring(expression.lastIndexOf("(") + 1, expression.lastIndexOf("(") + expression.substring(expression.lastIndexOf("(")).indexOf(")"));
            return associativeOperation(expression.replace("(" + multiExp + ")", String.valueOf(multipleOperation(multiExp))));
        }
        return multipleOperation(expression);
    }

    private void operate()
    {
        String text = screen.getText();

        // Get latest line
        while (text.contains("\n"))
        {
            text = text.substring(text.indexOf("\n") + 1, text.length());
        }

        // Remove whitespaces
        text = text.replaceAll(" ", "");

        /*
         *  Check valid expression
         */

        // Empty
        if (text.equals(""))
            screen.getText();

        // Unary operations
        else if (text.matches(unaryRegEx))
        {
            double result = unaryOperation(text);
            screen.setText(screen.getText() + "\n" + result);
            latestCommand.setText("" + result);
        }

        // Multiple operations
        else if (text.matches(multipleRegEx))
        {
            double result = multipleOperation(text);
            screen.setText(screen.getText() + "\n" + result);
            latestCommand.setText("" + result);            
        }

        // Associative operations
        else if (text.matches(associativeRegEx))
        {
            double result = associativeOperation(text);
            screen.setText(screen.getText() + "\n" + result);
            latestCommand.setText("" + result);  
        }

        // Malformed expression
        else {
            notifications.setForeground(Color.red);
            notifications.setText("Malformed expression!");
        }
    }

    public void actionPerformed(ActionEvent event)
    {
        notifications.setText("© Pleets");
        notifications.setForeground(Color.black);

        if (event.getSource() == one)
            screen.setText(screen.getText() + one.getText());
        else if (event.getSource() == two)
            screen.setText(screen.getText() + two.getText());
        else if (event.getSource() == three)
            screen.setText(screen.getText() + three.getText());
        else if (event.getSource() == four)
            screen.setText(screen.getText() + four.getText());
        else if (event.getSource() == five)
            screen.setText(screen.getText() + five.getText());
        else if (event.getSource() == six)
            screen.setText(screen.getText() + six.getText());
        else if (event.getSource() == seven)
            screen.setText(screen.getText() + seven.getText());
        else if (event.getSource() == eight)
            screen.setText(screen.getText() + eight.getText());        
        else if (event.getSource() == nine)
            screen.setText(screen.getText() + nine.getText());
        else if (event.getSource() == zero)
            screen.setText(screen.getText() + zero.getText());

        else if (event.getSource() == div)
            screen.setText(screen.getText() + "/");
        else if (event.getSource() == times)
            screen.setText(screen.getText() + "*");
        else if (event.getSource() == minus)
            screen.setText(screen.getText() + "-");
        else if (event.getSource() == plus)
            screen.setText(screen.getText() + "+");
        else if (event.getSource() == dot) 
        {
            if (screen.getText().trim().length() == 0)                     
                screen.setText("0.");
            else
            {
                String evalExp = screen.getText().concat(".0");
                if (evalExp.matches(multipleRegEx) || evalExp.concat(")").matches(associativeRegEx))
                    screen.setText(screen.getText()+".");
                else
                    notifications.setText("Invalid expression " + screen.getText().concat("."));
            }
        }
        else if (event.getSource() == mod)
            screen.setText(screen.getText() + "%");
        else if (event.getSource() == parL)
            screen.setText(screen.getText() + "(");
        else if (event.getSource() == parR)
            screen.setText(screen.getText() + ")");
        else if (event.getSource() == power)
            screen.setText(screen.getText() + "^");
        else if (event.getSource() == sqrt)
            screen.setText(screen.getText() + "√");

        else if (event.getSource() == clearE)
            screen.setText("");
        else if (event.getSource() == clear) {
            screen.setText("");
            latestCommand.setText("");
        }
        else if (event.getSource() == ans) {
            screen.setText(screen.getText() + latestCommand.getText());
        }
        else if (event.getSource() == equal) 
            operate();

    }

    public void keyTyped(KeyEvent event)
    {

    }

    public void keyPressed(KeyEvent event)
    {
        if (event.VK_ENTER == event.getKeyCode()) {
            operate();
            notifications.setText("Operation excecuted!");
        }
        if (event.VK_1 == event.getKeyCode() || 97 == event.getKeyCode())
            one.doClick();
        if (event.VK_2 == event.getKeyCode() || 98 == event.getKeyCode())
            two.doClick();
        if (event.VK_3 == event.getKeyCode() || 99 == event.getKeyCode())
            three.doClick();
        if (event.VK_4 == event.getKeyCode() || 100 == event.getKeyCode())
            four.doClick();
        if (event.VK_5 == event.getKeyCode() || 101 == event.getKeyCode())
            five.doClick();
        if (event.VK_6 == event.getKeyCode() || 102 == event.getKeyCode())
            six.doClick();
        if (event.VK_7 == event.getKeyCode() || 103 == event.getKeyCode())
            seven.doClick();
        if (event.VK_8 == event.getKeyCode() || 104 == event.getKeyCode())
            eight.doClick();
        if (event.VK_9 == event.getKeyCode() || 105 == event.getKeyCode())
            nine.doClick();
        if (event.VK_0 == event.getKeyCode() || 96 == event.getKeyCode())
            zero.doClick();
        if (106 == event.getKeyCode())
            times.doClick();        
        if (107 == event.getKeyCode())
            plus.doClick();
        if (109 == event.getKeyCode())
            minus.doClick();
        if (111 == event.getKeyCode())
            minus.doClick();
        if (46 == event.getKeyCode() || 110 == event.getKeyCode())
            dot.doClick();
        if (32 == event.getKeyCode())
            clearE.doClick();       
    }

    public void keyReleased(KeyEvent event) 
    {

    }

} 