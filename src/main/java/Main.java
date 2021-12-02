import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    JFrame window = new JFrame("Paint");
    window.setLayout(new FlowLayout());

    final JTextField x = new JTextField(3);
    final JTextField y = new JTextField(3);

    final Canvas canvas = new Canvas();

    canvas.setBackground(Color.LIGHT_GRAY);
    canvas.setPreferredSize(new Dimension(1200, 800));
    window.add(canvas);

    window.add(x);
    window.add(y);
    JButton moveBtn = new JButton("Move");
    window.add(moveBtn);
    JButton clearBtn = new JButton("Clear");
    window.add(clearBtn);
    JButton finishPolygonBtn = new JButton("Finish polygon");
    window.add(finishPolygonBtn);

    JTextField angle = new JTextField(3);
    window.add(angle);
    JButton rotateBtn = new JButton("Rotate");
    window.add(rotateBtn);

    JTextField scale = new JTextField(5);
    window.add(scale);
    JButton scaleBtn = new JButton("Scale");
    window.add(scaleBtn);
    JTextField point = new JTextField(5);
    window.add(point);
    JButton pointBtn = new JButton("Add point");
    window.add(pointBtn);

    ButtonGroup radioButtons = new ButtonGroup();
    JRadioButton createRadioBtn = new JRadioButton("Create polygon");
    createRadioBtn.setSelected(true);
    radioButtons.add(createRadioBtn);
    window.add(createRadioBtn);
    JRadioButton bezierRadioBtn = new JRadioButton("Curve");
    radioButtons.add(bezierRadioBtn);
    window.add(bezierRadioBtn);
    JRadioButton transformRadioBtn = new JRadioButton("Transform");
    radioButtons.add(transformRadioBtn);
    window.add(transformRadioBtn);
    JRadioButton anchorRadioBtn = new JRadioButton("Add anchor");
    radioButtons.add(anchorRadioBtn);
    window.add(anchorRadioBtn);
    JRadioButton mouseRotateBtn = new JRadioButton("Rotate with mouse");
    radioButtons.add(mouseRotateBtn);
    window.add(mouseRotateBtn);

    window.setSize(new Dimension(1200, 1000));
    window.setVisible(true);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    clearBtn.addActionListener(e -> canvas.clear());
    moveBtn.addActionListener(
        e -> canvas.moveSelected((Integer.parseInt(x.getText())), Integer.parseInt(y.getText())));
    finishPolygonBtn.addActionListener(e -> canvas.finishPolygon());
    bezierRadioBtn.addActionListener(
        e -> {
          canvas.setMode(CanvasOperation.BEZIER);
          canvas.updateTextFields(x, y, moveBtn);
        });
    createRadioBtn.addActionListener(e -> canvas.setMode(CanvasOperation.CREATE_NEW_POLYGON));
    transformRadioBtn.addActionListener(e -> canvas.setMode(CanvasOperation.TRANSFORM_POLYGON));
    anchorRadioBtn.addActionListener(e -> canvas.setMode(CanvasOperation.ADD_ANCHOR));
    rotateBtn.addActionListener(e -> canvas.rotateSelected(Integer.parseInt(angle.getText())));
    scaleBtn.addActionListener(
        e -> {
          final List<Double> scales =
              Arrays.stream(scale.getText().split(" "))
                  .map(Double::parseDouble)
                  .collect(Collectors.toList());

          canvas.scaleSelected(scales.get(0), scales.get(1));
        });
    mouseRotateBtn.addActionListener(e -> canvas.setMode(CanvasOperation.MOUSE_ROTATE));
    pointBtn.addActionListener(
        e -> {
          final List<Integer> coordinates =
              Arrays.stream(point.getText().split(" "))
                  .map(Integer::parseInt)
                  .collect(Collectors.toList());

          canvas.addPoint(coordinates.get(0), coordinates.get(1));
        });
  }
}
