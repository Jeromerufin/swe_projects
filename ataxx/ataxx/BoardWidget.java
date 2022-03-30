/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import static ataxx.PieceColor.*;
import static ataxx.Utils.*;

/** Widget for displaying an Ataxx board.
 *  @author Jerome Rufin
 */
class BoardWidget extends Pad  {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Number of squares on a side. */
    static final int SIDE = Board.SIDE;
    /** Radius of circle representing a piece. */
    static final int PIECE_RADIUS = 15;
    /** Dimension of a block. */
    static final int BLOCK_WIDTH = 40;

    /** Color of red pieces. */
    private static final Color RED_COLOR = Color.RED;
    /** Color of blue pieces. */
    private static final Color BLUE_COLOR = Color.BLUE;
    /** Color of painted lines. */
    private static final Color LINE_COLOR = Color.BLACK;
    /** Color of blank squares. */
    private static final Color BLANK_COLOR = Color.WHITE;
    /** Color of selected squared. */
    private static final Color SELECTED_COLOR = new Color(150, 150, 150);
    /** Color of blocks. */
    private static final Color BLOCK_COLOR = Color.BLACK;

    /** Stroke for lines. */
    private static final BasicStroke LINE_STROKE = new BasicStroke(1.0f);
    /** Stroke for blocks. */
    private static final BasicStroke BLOCK_STROKE = new BasicStroke(5.0f);

    /** Number of valid cols in board. */
    private static final char[] COLS = {'a', 'b', 'c', 'd', 'e', 'f', 'g'};

    /** Number of valid rows in board. */
    private static final char[] ROWS = {'7', '6', '5', '4', '3', '2', '1'};

    /** Create HashMap for all values mapped to linearized values. */
    private void mappedCenterPositions() {
        _centerMap = new HashMap<>();
        ArrayList<Integer[]> midPoints = new ArrayList<>();
        for (int i = SQDIM / 2; i < SQDIM * SIDE; i += SQDIM) {
            for (int j = SQDIM / 2; j < SQDIM * SIDE; j += SQDIM) {
                Integer[] midPoint = new Integer[2];
                midPoint[0] = i;
                midPoint[1] = j;
                midPoints.add(midPoint);
            }
        }

        ArrayList<String> boardValues = new ArrayList<>();
        for (int i = 0; i < ROWS.length; i += 1) {
            for (int j = 0; j < COLS.length; j += 1) {
                String s = "" + COLS[i] + ROWS[j];
                boardValues.add(s);
            }
        }

        for (int i = 0; i < boardValues.size(); i += 1) {
            _centerMap.put(boardValues.get(i), midPoints.get(i));
        }

    }

    /** A new widget sending commands resulting from mouse clicks
     *  to COMMANDQUEUE. */
    BoardWidget(ArrayBlockingQueue<String> commandQueue) {
        _commandQueue = commandQueue;
        mappedCenterPositions();
        setMouseHandler("click", this::handleClick);
        _dim = SQDIM * SIDE;
        _blockMode = false;
        setPreferredSize(_dim, _dim);
        setMinimumSize(_dim, _dim);
    }

    /** Indicate that SQ (of the form CR) is selected, or that none is
     *  selected if SQ is null. */
    void selectSquare(String sq) {
        if (sq == null) {
            _selectedCol = _selectedRow = 0;
        } else {
            _selectedCol = sq.charAt(0);
            _selectedRow = sq.charAt(1);
        }
        repaint();
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BLANK_COLOR);
        g.fillRect(0, 0, _dim, _dim);
        int squareDiff = SQDIM / 2;
        for (String s:_centerMap.keySet()) {
            drawBlock(g, _centerMap.get(s)[0], _centerMap.get(s)[1],
                    LINE_COLOR, squareDiff, false);
        }
        if (_selectedCol != 0) {
            g.setColor(SELECTED_COLOR);
            g.fillRect((_selectedCol - 'a') *  SQDIM,
                    SQDIM * SIDE - (_selectedRow - '1')
                            * SQDIM - SQDIM, SQDIM, SQDIM);
        }
        HashMap<Integer, String> allVals = _model.linearizedMap();
        for (int i: allVals.keySet()) {
            String val = allVals.get(i);
            char boardCol = val.charAt(0);
            char boardRow = val.charAt(1);
            int x = _centerMap.get(val)[0];
            int y = _centerMap.get(val)[1];
            int ovalX = x - (2 * PIECE_RADIUS / 2);
            int ovalY = y - (2 * PIECE_RADIUS / 2);
            if (_model.get(boardCol, boardRow) == RED) {
                g.setColor(RED_COLOR);
                g.drawOval(ovalX, ovalY, 2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
                g.fillOval(ovalX, ovalY, 2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
            } else if (_model.get(i) == BLUE) {
                g.setColor(BLUE_COLOR);
                g.drawOval(ovalX, ovalY, 2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
                g.fillOval(ovalX, ovalY, 2 * PIECE_RADIUS, 2 * PIECE_RADIUS);
            } else if (_model.get(i) == BLOCKED) {
                int squareBlockDiff = BLOCK_WIDTH / 2;
                g.setColor(BLOCK_COLOR);
                drawBlock(g, x, y, BLOCK_COLOR, squareBlockDiff, true);
            }
        }

    }

    /** Draw a block centered at (CX, CY) on G with COLOR,
     * SQUAREDIFF, and BLOCKSTROKE. */
    void drawBlock(Graphics2D g, int cx, int cy, Color
            color, int squareDiff, boolean blockStroke) {
        g.setColor(color);
        if (blockStroke) {
            g.setStroke(BLOCK_STROKE);
        } else {
            g.setStroke(LINE_STROKE);
        }
        g.drawLine(cx - squareDiff, cy - squareDiff,
                cx + squareDiff, cy - squareDiff);
        g.drawLine(cx - squareDiff, cy + squareDiff,
                cx - squareDiff, cy - squareDiff);
        g.drawLine(cx + squareDiff, cy - squareDiff,
                cx + squareDiff, cy + squareDiff);
        g.drawLine(cx + squareDiff, cy + squareDiff,
                cx - squareDiff, cy + squareDiff);
        if (blockStroke) {
            g.drawLine(cx - squareDiff, cy - squareDiff,
                    cx + squareDiff, cy + squareDiff);
            g.drawLine(cx - squareDiff, cy, cx + squareDiff, cy);
            g.drawLine(cx - squareDiff, cy + squareDiff,
                    cx + squareDiff, cy - squareDiff);
            g.drawLine(cx, cy + squareDiff, cx, cy - squareDiff);
        }
    }

    /** Clear selected block, if any, and turn off block mode. */
    void reset() {
        _selectedRow = _selectedCol = 0;
        setBlockMode(false);
    }

    /** Set block mode on iff ON. */
    void setBlockMode(boolean on) {
        _blockMode = on;
    }

    /** Issue move command indicated by mouse-click event WHERE. */
    private void handleClick(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        char mouseCol, mouseRow;
        if (where.getButton() == MouseEvent.BUTTON1) {
            mouseCol = (char) (x / SQDIM + 'a');
            mouseRow = (char) ((SQDIM * SIDE - y) / SQDIM + '1');
            if (mouseCol >= 'a' && mouseCol <= 'g'
                && mouseRow >= '1' && mouseRow <= '7') {
                if (_blockMode) {
                    String block = String.format("block %1$s%2$s",
                            mouseCol, mouseRow);
                    _commandQueue.offer(block);
                } else {
                    if (_selectedCol != 0) {
                        String move = String.format("%1$s%2$s-%3$s%4$s",
                                _selectedCol, _selectedRow, mouseCol, mouseRow);
                        _commandQueue.offer(move);
                        _selectedCol = _selectedRow = 0;
                    } else {
                        _selectedCol = mouseCol;
                        _selectedRow = mouseRow;
                    }
                }
            }
        }
        repaint();
    }

    public synchronized void update(Board board) {
        _model = new Board(board);
        repaint();
    }

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** Model being displayed. */
    private static Board _model;

    /** Coordinates of currently selected square, or '\0' if no selection. */
    private char _selectedCol, _selectedRow;

    /** True iff in block mode. */
    private boolean _blockMode;

    /** Destination for commands derived from mouse clicks. */
    private ArrayBlockingQueue<String> _commandQueue;

    /** Destination for commands derived from mouse clicks. */
    private HashMap<String, Integer[]> _centerMap;
}
