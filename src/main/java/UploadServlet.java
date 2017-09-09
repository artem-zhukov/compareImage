import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


@MultipartConfig(location = "D:\\temp", maxFileSize = 16 * 1024 * 1024, fileSizeThreshold = 0)
public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart1 = request.getPart("f1");
        Part filePart2 = request.getPart("f2");
        ImageIO.setCacheDirectory(new File("D:\\temp"));
        BufferedImage img1 = ImageIO.read(filePart1.getInputStream());
        filePart1.getInputStream().close();

        BufferedImage img2 = ImageIO.read(filePart2.getInputStream());
        filePart2.getInputStream().close();
        try {
            ImageIO.write(getDifferenceImage(img1, img2), "png", new File("D:\\temp\\image3.png"));
            Desktop.getDesktop().open(new File("D:\\temp\\image3.png"));
            response.sendRedirect("result.jsp");
        } catch (Exception e) {
            response.sendRedirect("/");
        }
    }

    private static BufferedImage getDifferenceImage(BufferedImage img1, BufferedImage img2) {
        int width1 = img1.getWidth();
        int width2 = img2.getWidth();
        int height1 = img1.getHeight();
        int height2 = img2.getHeight();
        if ((width1 != width2) || (height1 != height2)) {
            System.err.println("Error: Images dimensions mismatch");
            System.exit(1);
        }
        int diff;
        ArrayList<Integer> heightList = new ArrayList<>();
        ArrayList<Integer> widthList = new ArrayList<>();
        for (int i = 0; i < height1; i++) {
            for (int j = 0; j < width1; j++) {
                int rgb1 = img1.getRGB(j, i);
                int rgb2 = img2.getRGB(j, i);
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;
                diff = Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);
                if (diff != 0) {
                    heightList.add(i);
                    widthList.add(j);
                    Collections.sort(heightList);
                    Collections.sort(widthList);
                } else if (heightList.size() != 0 || widthList.size() != 0) {
                    if (i - heightList.get(heightList.size() - 1) > 1) {
                        highlightDiff(img1, heightList, widthList);
                        heightList.clear();
                        widthList.clear();
                    }
                }
            }
        }
        return img1;
    }

    private static void highlightDiff(BufferedImage img, ArrayList<Integer> heightList, ArrayList<Integer> widthList) {
        for (int i = heightList.get(0); i < heightList.get(heightList.size() - 1); i++) {
            img.setRGB(widthList.get(0), i, Color.RED.getRGB());
            img.setRGB(widthList.get(widthList.size() - 1), i, Color.RED.getRGB());
        }
        for (int i = widthList.get(0); i < widthList.get(widthList.size() - 1); i++) {
            img.setRGB(i, heightList.get(0), Color.RED.getRGB());
            img.setRGB(i, heightList.get(heightList.size() - 1), Color.RED.getRGB());
        }
    }
}
