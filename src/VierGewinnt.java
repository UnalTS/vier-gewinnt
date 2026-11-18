import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public final class VierGewinnt extends JPanel {
    static final Color NIGHT = new Color(19, 29, 48), PANEL = new Color(38, 54, 80);
    static final Color LIGHT = new Color(244, 246, 250), MUTED = new Color(174, 191, 216);
    static final Color RED = new Color(247, 63, 69), YELLOW = new Color(255, 225, 48);
    private Game game = new Game();
    private final JLabel status = new JLabel();
    private final Board board = new Board();
    private final Timer timer = new Timer(15, e -> tick());
    private int column = 3, fallingRow = -1, fallingColumn, fallingPlayer;
    private long started;
    private double progress;

    VierGewinnt() {
        super(new BorderLayout(0, 12));
        setBackground(NIGHT);
        setBorder(new EmptyBorder(22, 28, 18, 28));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = label("Vier gewinnt", 32, LIGHT);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        header.add(title, BorderLayout.WEST);
        JButton restart = new JButton("Neue Runde");
        restart.setFont(new Font("Dialog", Font.BOLD, 14));
        restart.setBackground(PANEL);
        restart.setForeground(LIGHT);
        restart.setFocusPainted(true);
        restart.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MUTED), new EmptyBorder(7, 14, 7, 14)));
        restart.addActionListener(e -> reset());
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        actions.add(restart);
        header.add(actions, BorderLayout.EAST);
        status.setFont(new Font("Dialog", Font.BOLD, 16));
        status.setBorder(new EmptyBorder(8, 0, 0, 0));
        header.add(status, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);
        add(board, BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 3));
        controls.setOpaque(false);
        controls.add(keycap("←"));
        controls.add(keycap("→"));
        JLabel chooseHint = label("Spalte wählen", 13, LIGHT);
        chooseHint.setBorder(new EmptyBorder(0, 2, 0, 14));
        controls.add(chooseHint);
        controls.add(keycap("↵ Enter"));
        controls.add(label("Stein setzen", 13, LIGHT));
        add(controls, BorderLayout.SOUTH);
        updateStatus();
    }

    private static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(new Font("Dialog", Font.PLAIN, size));
        return label;
    }

    private static JLabel keycap(String text) {
        JLabel key = label(text, 14, LIGHT);
        key.setFont(key.getFont().deriveFont(Font.BOLD));
        key.setOpaque(true);
        key.setBackground(PANEL);
        key.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 3, 1, MUTED), new EmptyBorder(3, 8, 3, 8)));
        return key;
    }

    private static Color color(int player) { return player == 1 ? RED : YELLOW; }
    private static String name(int player) { return player == 1 ? "Rot" : "Gelb"; }

    private void updateStatus() {
        String text = name(game.player()) + " ist am Zug";
        if (!game.winningLine().isEmpty()) text = name(game.player()) + " gewinnt!";
        else if (game.finished()) text = "Unentschieden";
        status.setText(text);
        status.setForeground(game.finished() && game.winningLine().isEmpty() ? LIGHT : color(game.player()));
    }

    private void reset() {
        timer.stop();
        game = new Game();
        column = 3;
        fallingRow = -1;
        updateStatus();
        board.repaint();
        board.requestFocusInWindow();
    }

    private void play() {
        if (fallingRow >= 0) return;
        fallingPlayer = game.player();
        fallingRow = game.drop(column);
        if (fallingRow < 0) return;
        fallingColumn = column;
        started = System.nanoTime();
        progress = 0;
        timer.start();
        board.repaint();
    }

    private void tick() {
        progress = Math.min(1, (System.nanoTime() - started) / 260_000_000.0);
        if (progress >= 1) {
            fallingRow = -1;
            timer.stop();
            updateStatus();
        }
        board.repaint();
    }

    private final class Board extends JPanel {
        Board() {
            setOpaque(false);
            setFocusable(true);
            getAccessibleContext().setAccessibleName("Spielfeld");
            setPreferredSize(new Dimension(490, 452));
            MouseAdapter mouse = new MouseAdapter() {
                public void mouseMoved(MouseEvent e) { select(e); }
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    if (SwingUtilities.isLeftMouseButton(e) && select(e)) play();
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
            bind("LEFT", () -> choose(Math.max(0, column - 1)));
            bind("RIGHT", () -> choose(Math.min(Game.COLUMNS - 1, column + 1)));
            bind("ENTER", () -> play());
        }

        private void bind(String key, Runnable action) {
            getInputMap().put(KeyStroke.getKeyStroke(key), key);
            getActionMap().put(key, new AbstractAction() {
                public void actionPerformed(ActionEvent e) { action.run(); }
            });
        }

        private int step() { return Math.max(1, Math.min((getWidth()-16)/7, (getHeight()-36)/6)); }
        private int left() { return (getWidth()-7*step())/2; }
        private int top() { return (getHeight()-6*step()+24)/2; }

        private boolean select(MouseEvent e) {
            int x = e.getX() - left(), y = e.getY() - top();
            if (x < 0 || x >= 7*step() || y < -24 || y >= 6*step()) return false;
            choose(x / step());
            return true;
        }

        private void choose(int next) {
            if (fallingRow < 0 && !game.finished()) column = next;
            repaint();
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int s = step(), x = left(), y = top();
            g.setColor(PANEL);
            g.fillRoundRect(x-6, y-6, 7*s+12, 6*s+12, 24, 24);
            if (fallingRow < 0 && !game.finished()) {
                int mid = x + column*s + s/2;
                g.setColor(color(game.player()));
                g.fillPolygon(new int[]{mid-6, mid+6, mid}, new int[]{y-20,y-20,y-12}, 3);
            }
            for (int r = 0; r < Game.ROWS; r++) {
                for (int c = 0; c < Game.COLUMNS; c++) {
                    int p = game.at(r,c);
                    if (r == fallingRow && c == fallingColumn) p = 0;
                    disc(g, x+c*s, y+r*s, s, p, fallingRow < 0 && game.winningLine().contains(new Game.Cell(r,c)));
                }
            }
            if (fallingRow >= 0) {
                g.clipRect(x, y, 7*s, 6*s);
                double row = -1 + (fallingRow+1)*progress*progress;
                disc(g, x+fallingColumn*s, y+row*s, s, fallingPlayer, false);
            }
            g.dispose();
        }

        private void disc(Graphics2D g, double x, double y, int s, int p, boolean winner) {
            double pad = s * 0.105, diameter = s - 2*pad;
            Ellipse2D circle = new Ellipse2D.Double(x+pad,y+pad,diameter,diameter);
            g.setColor(p == 0 ? NIGHT : color(p));
            g.fill(circle);
            if (p != 0) {
                double inset = s * 0.19;
                g.setColor(new Color(0,0,0,38));
                g.setStroke(new BasicStroke(1.6f));
                g.draw(new Ellipse2D.Double(x+inset,y+inset,s-2*inset,s-2*inset));
            }
            if (winner) {
                g.setColor(LIGHT);
                g.setStroke(new BasicStroke(4f));
                g.draw(circle);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Vier gewinnt");
            VierGewinnt app = new VierGewinnt();
            window.setContentPane(app);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.pack();
            window.setMinimumSize(new Dimension(430, 460));
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            app.board.requestFocusInWindow();
        });
    }
}
