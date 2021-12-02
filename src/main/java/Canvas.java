import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Canvas extends JPanel {
  private final List<Shape> shapes = new ArrayList<>();
  private Polygon selected;
  private Polygon newShape;
  private CanvasOperation mode = CanvasOperation.CREATE_NEW_POLYGON;
  private Point anchor;
  private boolean pressedOnSelected;
  private int theta;
  private final double STEPS = 5000.0;
  private final List<Point> points = new ArrayList<>();
  private final List<Point> bezierCurvePoints = new ArrayList<>();
  private Point bezierSelected;

  public Canvas() {
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {

            if (CanvasOperation.BEZIER.equals(mode)) {
              if (bezierSelected != null) {
                bezierSelected = null;
              } else {
                final Optional<Point> point =
                    points.stream()
                        .filter(p -> new Rectangle(p.x - 3, p.y - 3, 12, 12).contains(e.getPoint()))
                        .findFirst();
                if (point.isPresent()) {
                  bezierSelected = point.get();
                } else {
                  points.add(new Point(e.getX(), e.getY()));
                }
              }
            }

            if (CanvasOperation.TRANSFORM_POLYGON.equals(mode)) {
              for (Shape s : shapes) {
                if (s.contains(e.getX(), e.getY())) {
                  if (selected == null) {
                    selected = ((Polygon) s);
                  } else {
                    selected = null;
                  }
                }
              }
            }

            if (CanvasOperation.CREATE_NEW_POLYGON.equals(mode) && selected == null) {
              if (newShape == null) {
                newShape = new Polygon();
                shapes.add(newShape);
              }
              newShape.addPoint(e.getX(), e.getY());
            }

            if (CanvasOperation.CREATE_NEW_POLYGON.equals(mode) && selected != null) {
              selected.addPoint(e.getX(), e.getY());
            }

            if (CanvasOperation.TRANSFORM_POLYGON.equals(mode)
                && shapes.stream().noneMatch(s -> s.contains(e.getX(), e.getY()))) {
              selected = null;
            }

            if (CanvasOperation.ADD_ANCHOR.equals(mode)) {
              anchor = new Point(e.getX(), e.getY());
            }

            repaint();
          }

          @Override
          public void mousePressed(MouseEvent e) {
            if (bezierSelected != null
                && new Rectangle(bezierSelected.x - 3, bezierSelected.y - 3, 12, 12)
                    .contains(e.getPoint())) {
              pressedOnSelected = true;
            } else if (selected != null) {
              pressedOnSelected = true;
              if (anchor != null) {
                theta = getTheta(e);
              }
            }
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            if (pressedOnSelected) {
              pressedOnSelected = false;
              theta = 0;
            }
          }
        });

    addMouseMotionListener(
        new MouseAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
            if (CanvasOperation.BEZIER.equals(mode) && pressedOnSelected) {
              bezierSelected.setLocation(e.getPoint());
            } else if (CanvasOperation.TRANSFORM_POLYGON.equals(mode) && pressedOnSelected) {
              selected.translate(e.getX() - selected.xpoints[0], e.getY() - selected.ypoints[0]);
            } else {
              if (anchor != null) {
                int newTheta = getTheta(e);
                final int angle = newTheta - theta;
                theta = newTheta;
                if (angle != 0) {
                  rotateSelected(angle);
                }
              }
            }
            repaint();
          }
        });
  }

  private int getTheta(MouseEvent e) {
    return (int) (180.0 / Math.PI * Math.atan2(anchor.x - e.getX(), e.getY() - anchor.y));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    bezierCurvePoints.clear();
    bezier(1 / STEPS);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setPaint(Color.BLACK);
    points.forEach(p -> g2d.fillOval(p.getLocation().x, p.getLocation().y, 6, 6));
    bezierCurvePoints.forEach(p -> g2d.fillOval(p.getLocation().x, p.getLocation().y, 1, 1));
    if (bezierSelected != null) {
      g2d.setPaint(Color.BLACK);
      g2d.drawRect(bezierSelected.x - 3, bezierSelected.y - 3, 12, 12);
    }
    shapes.forEach(g2d::draw);

    if (selected != null) {
      g2d.setPaint(Color.RED);
      g2d.draw(selected);
    }

    if (anchor != null) {
      g2d.setPaint(Color.MAGENTA);
      g2d.fillOval(anchor.x - 5, anchor.y - 5, 10, 10);
    }
  }

  public void setMode(CanvasOperation mode) {
    newShape = null;
    this.mode = mode;
    repaint();
  }

  public void moveSelected(int x, int y) {
    if (selected != null) {
      selected.translate(x, y);
      repaint();
    }
    if (bezierSelected != null) {
      bezierSelected.setLocation(x, y);
      bezierSelected.translate(x,y);
      repaint();
    }
  }

  public void rotateSelected(int angle) {
    if (selected != null) {
      final int[] xpoints = selected.xpoints;
      final int[] ypoints = selected.ypoints;
      final int npoints = selected.npoints;

      int[] resultXPoints = new int[npoints];
      int[] resultYPoints = new int[npoints];

      for (int i = 0; i < npoints; i++) {
        resultXPoints[i] = rotateX(angle, xpoints[i], ypoints[i], anchor.x, anchor.y);
        resultYPoints[i] = rotateY(angle, xpoints[i], ypoints[i], anchor.x, anchor.y);
      }

      selected.xpoints = resultXPoints;
      selected.ypoints = resultYPoints;
      repaint();
    }
  }

  private int rotateX(int angle, int shapeX, int shapeY, int anchorX, int anchorY) {
    return (int)
        (anchorX
            + (shapeX - anchorX) * Math.cos(Math.toRadians(angle))
            - (shapeY - anchorY) * Math.sin(Math.toRadians(angle)));
  }

  private int rotateY(int angle, int shapeX, int shapeY, int anchorX, int anchorY) {
    return (int)
        (anchorY
            + (shapeX - anchorX) * Math.sin(Math.toRadians(angle))
            + (shapeY - anchorY) * Math.cos(Math.toRadians(angle)));
  }

  public void scaleSelected(double scaleX, double scaleY) {
    if (CanvasOperation.TRANSFORM_POLYGON.equals(mode) && selected != null) {
      final int[] xpoints = selected.xpoints;
      final int[] ypoints = selected.ypoints;
      final int npoints = selected.npoints;

      int[] resultXPoints = new int[npoints];
      int[] resultYPoints = new int[npoints];

      for (int i = 0; i < npoints; i++) {
        resultXPoints[i] = scaleX(scaleX, xpoints[i], anchor.x);
        resultYPoints[i] = scaleY(scaleY, ypoints[i], anchor.y);
      }

      selected.xpoints = resultXPoints;
      selected.ypoints = resultYPoints;
      repaint();
    }
  }

  private int scaleX(double scaleX, int shapeX, int anchorX) {
    return (int) (anchorX + (shapeX - anchorX) * scaleX);
  }

  private int scaleY(double scaleY, int shapeY, int anchorY) {
    return (int) (anchorY + (shapeY - anchorY) * scaleY);
  }

  public void finishPolygon() {
    newShape = null;
  }

  public void clear() {
    selected = null;
    anchor = null;
    bezierCurvePoints.clear();
    points.clear();
    shapes.clear();
    repaint();
    bezierSelected = null;
    pressedOnSelected = false;
  }

  public void addPoint(Integer x, Integer y) {
    if (selected != null) {
      selected.addPoint(x, y);
    } else {
      if (newShape == null) {
        newShape = new Polygon();
        shapes.add(newShape);
      }
      newShape.addPoint(x, y);
    }
    repaint();
  }

  private void bezier(double step) {
    int xEnd = points.stream().map(p -> p.x).max(Integer::compareTo).orElse(1000);
    int degree = points.size() - 1;
    for (double p = 0; p <= 1; p += step) {
      double xPoint = 0;
      for (int i = 0; i <= points.size() - 1; ++i) {
        double newton = calculateNewton(degree, i);
        double ti = Math.pow(p, i);
        double lti = Math.pow((1 - p), (degree - i));
        xPoint = xPoint + (newton * ti * lti * points.get(i).x);
      }
      if (xPoint > xEnd) {
        break;
      }
      double yPoint = 0;
      for (int i = 0; i <= degree; ++i) {
        double yNewton = calculateNewton(degree, i);
        double yti = Math.pow(p, i);
        double ylti = Math.pow((1 - p), (degree - i));
        yPoint = yPoint + (yNewton * yti * ylti * points.get(i).y);
      }
      bezierCurvePoints.add(new Point((int) xPoint, (int) yPoint));
    }
  }

  private double calculateNewton(int n, int k) {
    long result = 1;
    for (int i = 1; i <= k; i++) {
      result = result * (n - i + 1) / i;
    }
    return result;
  }

  public Point getBezierSelected() {
    return bezierSelected;
  }

  public void updateTextFields(JTextField x, JTextField y, JButton move) {
    if (bezierSelected != null) {
      x.setText(String.valueOf(bezierSelected.x));
      y.setText(String.valueOf(bezierSelected.y));
    } else {
      x.setText("");
      y.setText("");
    }
  }
}
