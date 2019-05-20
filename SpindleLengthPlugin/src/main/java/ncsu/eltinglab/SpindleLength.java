/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package ncsu.eltinglab;

import java.awt.Color;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ImageCanvas;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

/**
 * A template for processing each pixel of either
 * GRAY8, GRAY16, GRAY32 or COLOR_RGB images.
 *
 * @author Johannes Schindelin
 * @author Ana Sofia Uzsoy
 */
public class SpindleLength implements PlugInFilter {
	protected ImagePlus image;

	// image property members
	private int width;
	private int height;

	// plugin parameters
	public double value;
	public String name;

	
	@Override
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}

		image = imp;
		return DOES_8G | DOES_16 | DOES_32 | DOES_RGB;
	}

	@Override
	public void run(ImageProcessor ip) {
		// get width and height
		width = ip.getWidth();
		height = ip.getHeight();

		if (showDialog()) {
			process(ip);
			image.updateAndDraw();
		}
	}

	private boolean showDialog() { // to hijack later for pop-up menu, etc
//		GenericDialog gd = new GenericDialog("Process pixels");
//
//		// default value is 0.00, 2 digits right of the decimal point
//		gd.addNumericField("value", 0.00, 2);
//		gd.addStringField("name", "John");
//
//		gd.showDialog();
//		if (gd.wasCanceled())
//			return false;

//		// get entered values
//		value = gd.getNextNumber();
//		name = gd.getNextString();
//		
		
		return true;
	}

	/**
	 * Process an image.
	 * <p>
	 * Please provide this method even if {@link ij.plugin.filter.PlugInFilter} does require it;
	 * the method {@link ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)} can only
	 * handle 2-dimensional data.
	 * </p>
	 * <p>
	 * If your plugin does not change the pixels in-place, make this method return the results and
	 * change the {@link #setup(java.lang.String, ij.ImagePlus)} method to return also the
	 * <i>DOES_NOTHING</i> flag.
	 * </p>
	 *
	 * @param image the image (possible multi-dimensional)
	 */
	public void process(ImagePlus image) {
		// slice numbers start with 1 for historical reasons
		for (int i = 1; i <= image.getStackSize(); i++)
			process(image.getStack().getProcessor(i));
	}

	// Select processing method depending on image type
	public void process(ImageProcessor ip) {
		int type = image.getType();
		if (type == ImagePlus.GRAY8)
			process( (byte[]) ip.getPixels());
		else if (type == ImagePlus.GRAY16)
			process( (short[]) ip.getPixels() );
		else if (type == ImagePlus.GRAY32)
			process( (float[]) ip.getPixels() );
		else if (type == ImagePlus.COLOR_RGB)
			process( (int[]) ip.getPixels() ) ;
		else {
			throw new RuntimeException("not supported");
		}
	}

	// processing of GRAY8 images
	public void process(byte[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (byte)value;
			}
		}
	}

	// processing of GRAY16 images
	public void process(short[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (float)value;
			}
		}
	}

	// processing of GRAY32 images
	public void process(float[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (float)value;
			}
		}
	}

	// processing of COLOR_RGB images
	public void process(int[] pixels) {
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				// process each pixel of the line
				// example: add 'number' to each pixel
				pixels[x + y * width] += (int)value;
			}
		}
	}

	public void showAbout() {
		IJ.showMessage("SpindleLength",
			"measures spindle length over time of dividing cells"
		);
	}

	public static double getLength(ImagePlus im, RoiManager m) {
		
		ImageProcessor proc = im.getProcessor();
		
		// normalize all images (with linear transformation) so they go from 0 to 65000

		double maximum = Integer.MIN_VALUE;
		double minimum = Integer.MAX_VALUE;
		
		for (int i = 0; i < proc.getWidth(); i++) {
			for (int j = 0; j < proc.getHeight(); j++) {
				if (proc.get(i,j) > maximum) {
					maximum = proc.get(i, j);
				}
				if (proc.get(i,j) < minimum) {
					minimum = proc.get(i, j);
				}
				
			}
		}
		
		System.out.println("maximum: " + maximum);
		System.out.println("maximum: " + minimum);

		
		double slope = 65000.0 / (maximum - minimum);
		double intercept = -1 * slope * minimum;
		
		for (int i = 0; i < proc.getWidth(); i++) {
			for (int j = 0; j < proc.getHeight(); j++) {
				int old = proc.get(i,j);
				double n = slope * old + intercept;
				//System.out.println(old + ", " + (int) n);
				proc.set(i, j, (int) n);
			}
		}
		
		// Making a giant comma-separated list of pixel values to pass into the 
		// Python script as a fake command line argument
		StringBuilder pixelString = new StringBuilder("");
		for (int i = 0; i < proc.getWidth(); i++) {
			for (int j = 0; j < proc.getHeight(); j++) {
				pixelString.append(proc.get(i, j) + ",");
			}
		}
		pixelString.deleteCharAt(pixelString.length() - 1); //removes trailing comma
		
		
		// runs python script to interpolate a threshold based on the pixel array
		String s1 = null;
		String s2 = null;
		double thresh = 0.0;
		try {
			Process p = Runtime.getRuntime().exec("python python/newinterpolation.py " + pixelString);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s1 = stdInput.readLine()) != null) {
                s2 = s1;
            }
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		thresh = Double.valueOf(s2);
		
		
		System.out.println("threshold: " + thresh);
		
		// sets all pixels below the threshold to zero intensity
		
		for (int i = 0; i < proc.getWidth(); i++) {
			for (int j = 0; j < proc.getHeight(); j++) {
				if (proc.get(i, j) < thresh) {
					proc.set(i, j, 0);
				} else {
					System.out.println("larger");
				}
			}
		}
		
		// checks for isolated random bright pixels and sets them to zero
		for (int i = 0; i < proc.getWidth() - 1; i++) {
			for (int j = 0; j < proc.getHeight() - 1; j++) {
				if (proc.get(i, j) != 0 ) {
					if (proc.get(i, j + 1) == 0 && proc.get(i, j - 1) == 0) {
						if (proc.get(i + 1, j) == 0 && proc.get(i - 1, j) == 0) {
							proc.set(i, j, 0);
						}
					}
				}
				
			}
		}
		
		//im.show();
		// calculates center of mass coordinates
		double xsum = 0;
		double ysum = 0;
		double mass = 0.0;
		for (int i = 1; i < proc.getWidth(); i++) {
			for (int j = 1; j < proc.getHeight(); j++) {
				mass += proc.get(i, j);
				xsum += proc.get(i, j) * i;
				ysum += proc.get(i, j) * j;
				//System.out.println("hello");
			}
		}
		
		double xcm = xsum/mass;
		double ycm = ysum/mass;

		System.out.println("Center of mass: (" + xcm + "," + ycm + ")");

		// calculates moment of inertia tensor "matrix"
		double Ixx = 0;
		double Iyy = 0;
		double Ixy = 0;
		for (int i = 0; i < proc.getWidth(); i++) {
			for (int j = 0; j < proc.getHeight(); j++) {
				Ixx = Ixx + proc.get(i, j) * (i - xcm) * (i - xcm);
				Iyy = Iyy + proc.get(i, j) * (j - ycm) * (j - ycm);
				Ixy = Ixy + proc.get(i, j) * (i - xcm) * (j - ycm);
			}
		}
		
		String matrixString = Ixx + "," + Ixy + "," + Ixy + "," + Iyy;

		// passes in matrix string to python script which outputs principal eigenvectors
		double xvector = 1.0;
		double yvector = 1.0;
		try {
			Process p = Runtime.getRuntime().exec("python python/lazylinalg.py " + matrixString);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            xvector = Double.valueOf(stdInput.readLine());
            yvector = Double.valueOf(stdInput.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("x vector: " + xvector);
		System.out.println("y vector: " + yvector);
		
		// backtrack vector from center of mass to edge of image
		double x = xcm;
		double y = ycm;
		while ((x > 0 && y > 0) && (x < (proc.getWidth() - Math.abs(xvector)) && y < (proc.getHeight() - Math.abs(yvector)))) {
			x += xvector;
			y += yvector;
		}

		double bottomx = x;
		double bottomy = y;
		
		System.out.println("bottomx: " + bottomx);
		System.out.println("bottomy: " + bottomy);
		
		ArrayList<Integer> intensities = new ArrayList<Integer>();
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Double> pointsx = new ArrayList<Double>();
		ArrayList<Double> pointsy = new ArrayList<Double>();
		// iterates over pixels in line through spindle across the frame, adding them to above arrays
		int index = 0;
		do {
			x += -1 * xvector;
			y += -1 * yvector;
//			
//			System.out.println("x: " + x);
//			System.out.println("y: " + y);
			intensities.add(proc.get((int) x,(int) y)); 
//			try {
//				proc.set((int) x, (int) y, 65000); // this modifies the actual image 
//				// so we should use this only for visualization purposes
//			} catch (Exception e) {
//				//do nothing
//			}
			indexes.add(index);
			index++;
			pointsx.add(x);
			pointsy.add(y);
		} while ((x > (-1 * Math.abs(xvector)) && y > (-1 * Math.abs(yvector))) && 
				(x < (proc.getWidth() - Math.abs(xvector)) && y < (proc.getHeight() - Math.abs(yvector))));
		
		// go through the intensities to find the ends (first instance where intensity is
		// not equal to zero) starting from the front and back of the array.
		int minindex = 0;
		int maxindex = 0;
		for (int i = 0; i < intensities.size(); i++) {
			if (intensities.get(i) != 0) {
				minindex = indexes.get(i);
				break;
			}
		}
		for (int i = intensities.size() - 1; i >= 0; i--) {
			if (intensities.get(i) != 0) {
				maxindex = indexes.get(i);
				break;
			}
		}
		im.show();
		
		// add red ROI circles on the ends of the spindle and add them to the ROI manager.
		Vector<Roi> displayList = new Vector<Roi>();
		Roi circle = new OvalRoi(pointsx.get(minindex), pointsy.get(minindex), 5, 5);
		circle.setFillColor(Color.RED);
		Roi circle2 = new OvalRoi(pointsx.get(maxindex), pointsy.get(maxindex), 5, 5);
		circle2.setFillColor(Color.RED);
		displayList.add(circle);
		displayList.add(circle2);
		ImageCanvas c = im.getCanvas();
		c.setDisplayList(displayList);
		m.addRoi(circle);
		m.addRoi(circle2);
		
		double deltax = Math.abs(pointsx.get(minindex) - pointsx.get(maxindex));
		double deltay = Math.abs(pointsy.get(minindex) - pointsy.get(maxindex));

		
		// calculate and return spindle length
		double length = Math.sqrt(deltax * deltax + deltay * deltay);
		return length;

	}
	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = SpindleLength.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open the image
		String imageName = "input/Stack.tif";
		ImagePlus stack = IJ.openImage(imageName);
		System.out.println("Stack size: " + stack.getStackSize());
		
		ImageStack empty = stack.createEmptyStack();
		RoiManager manager = new RoiManager();
		
		ArrayList<Double> lengths = new ArrayList<Double>();
		ArrayList<Integer> frames = new ArrayList<Integer>();
		
		// go through all frames in the movie and record the spindle length in each one
		for (int framenum = 1; framenum <= stack.getStackSize(); framenum++) {
			ImagePlus frame = IJ.openImage(imageName, framenum);
			//frame.show();
			System.out.println("Frame number: " + framenum);
			double length = -1; // the length will stay negative if the algorithm can't measure it
			try {
				length = getLength(frame, manager);
				System.out.println("Length: " + length);
			} catch (Exception e) {
				System.out.println("Had trouble measuring frame " + framenum + " :(");
			}
			empty.addSlice(frame.getProcessor());
			lengths.add(length + 5);
			frames.add(framenum);
			try {
				TimeUnit.SECONDS.sleep(1); // display each frame for 1 second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			frame.close(); // close image that is opened in the getLength() method
		}
		
		// write data to output file here
		File f = new File("output/lengths.csv");
		PrintStream out = null;
		try {
			out = new PrintStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < frames.size(); i++) {
			out.println(frames.get(i) + "," + lengths.get(i));
		}
		out.close();
		
		IJ.runPlugIn(clazz.getName(), "");
		stack.show(); // this outputs the whole stack that you can scroll through
	}
}
