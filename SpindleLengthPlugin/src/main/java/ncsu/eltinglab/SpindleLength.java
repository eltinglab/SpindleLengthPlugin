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
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
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
//
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

	public static double getLength(ImagePlus im, RoiManager m, double progress) {
		
		ImagePlus im2 = im.duplicate();
		ImageProcessor proc = im2.getProcessor();

		
		// normalize all images (with linear transformation) so they go from 0 to 65000

		double maximum = Integer.MIN_VALUE;
		double minimum = Integer.MAX_VALUE;
		
		//im.show();
		
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
				proc.set(i, j, (int) n);
			}
		}
		
		
		ImageProcessor proc2 = proc.duplicate(); // keep a copy of the original for later
		
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
		System.out.println(pixelString.length());
		try {
			Process p = Runtime.getRuntime().exec("python python/newinterpolation.py " + pixelString);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((s1 = stdInput.readLine()) != null) {
                s2 = s1;
            }
            p.destroy();
		
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
				} 
			}
		}
		
		// checks for isolated random bright pixels and sets them to zero
		if (progress < 0.8) {
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
			}
		}
		
		double xcm = xsum/mass;
		double ycm = ysum/mass;

		// System.out.println("Center of mass: (" + xcm + "," + ycm + ")");

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
		
		double minorx = 1.0;
		double minory = 1.0;
		try {
			Process p = Runtime.getRuntime().exec("python python/lazylinalg.py " + matrixString);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            xvector = Double.valueOf(stdInput.readLine());
            yvector = Double.valueOf(stdInput.readLine());
            minorx = Double.valueOf(stdInput.readLine());
            minorx = Double.valueOf(stdInput.readLine());
            p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		System.out.println("x vector: " + xvector);
//		System.out.println("y vector: " + yvector);
		
		// backtrack vector from center of mass to edge of image
		double x = xcm;
		double y = ycm;
		while ((x > 0 && y > 0) && (x < (proc.getWidth() - Math.abs(xvector)) && y < (proc.getHeight() - Math.abs(yvector)))) {
			x += xvector;
			y += yvector;
		}

//		double bottomx = x;
//		double bottomy = y;
//		
//		System.out.println("bottomx: " + bottomx);
//		System.out.println("bottomy: " + bottomy);
		
		ArrayList<Integer> intensities = new ArrayList<Integer>();
		ArrayList<Integer> intensities_integrate = new ArrayList<Integer>();
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Double> pointsx = new ArrayList<Double>();
		ArrayList<Double> pointsy = new ArrayList<Double>();
		// iterates over pixels in line through spindle across the frame, integrates over a 
		// width of two spindles, adding intensities to above arrays
		int index = 0;
		int pivot = 0;
		do {
			x += -1 * xvector;
			y += -1 * yvector;
//		
//			System.out.println("x: " + x);
//			System.out.println("y: " + y);
			
			intensities.add(proc.get((int) x,(int) y));

			int intensity = proc2.get((int) x,(int) y) / 1000;
			if ((x + 2 * minorx < proc.getWidth() && x + 2 * minorx > 0) && (x - 2 * minorx < proc.getWidth() && x - 2 * minorx > 0)) {
				if ((y + 2 * minory < proc.getHeight() && y + 2 * minory > 0) && (y - 2 * minory < proc.getHeight() && y - 2 * minory > 0)) {
					intensity += (proc2.get((int)(x + minorx), (int) (y + minory)) / 1000);
					intensity += (proc2.get((int)(x - minorx), (int) (y - minory)) / 1000);	
					intensity += (proc2.get((int)(x + 2 * minorx), (int) (y + 2 * minory)) / 1000);
					intensity += (proc2.get((int)(x - 2 * minorx), (int) (y - 2 * minory)) / 1000);	
				}
			}
			
			intensities_integrate.add(intensity);
			if ((int) x == (int) xcm) {
				pivot = index;
			}
			
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
		//im.show();
		
		// runs curve fitting code
		StringBuilder intenseString = new StringBuilder("");
		StringBuilder indexString = new StringBuilder("");
		for (int i = 0; i < intensities.size(); i++) {
			intenseString.append(intensities_integrate.get(i) + ",");
			indexString.append(indexes.get(i) + ",");
		}
		
		intenseString.deleteCharAt(intenseString.length() - 1);
		indexString.deleteCharAt(indexString.length() - 1); // removes trailing comma

		double rsquared = 0.0;
		double l = -1.0;
		int oneend = 0;
		int otherend = 0;
		try {
			Process p = Runtime.getRuntime().exec("python python/curvefit2.py " + indexString + " " + intenseString + " " + pivot);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            //System.out.println(stdInput.readLine());
			try {
				rsquared = Double.valueOf(stdInput.readLine());
	            l = Double.valueOf(stdInput.readLine());
	            oneend = Integer.valueOf(stdInput.readLine());
	            otherend = Integer.valueOf(stdInput.readLine());
			} catch (Exception e) { // there was an error in the curve fitting
				//do nothing
			}
			
            p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("R squared: " + rsquared);
		System.out.println("Curve fit length: " + l);
        System.out.println(oneend + " " + otherend);

		// calculate spindle length
        // uses curve fit length only if r squared is greater than 0.75
		double deltax = Math.abs(pointsx.get(minindex) - pointsx.get(maxindex));
		double deltay = Math.abs(pointsy.get(minindex) - pointsy.get(maxindex));

		double length = Math.sqrt(deltax * deltax + deltay * deltay);

		if (rsquared >= 0.75 && l < proc.getHeight() && l < proc.getWidth()) {
			minindex = oneend;
			maxindex = otherend;
			length = l;
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

		
		// prompts user for filename
		GenericDialog gd = new GenericDialog("Enter file name");
		String filename = "output/lengths.csv";
		gd.addStringField("Enter file name", filename);
		gd.showDialog();
		filename = gd.getNextString();
		if (gd.wasCanceled()) {
		  System.exit(1);
		}
		
		// asks about scaling, records factor if user specifies one
		double factor = 0.0;
		GenericDialog askToScale = new GenericDialog("Scaling");
		askToScale.addMessage("Would you like to scale your image?");
		askToScale.setCancelLabel("No");
		askToScale.setOKLabel("Yes");
		askToScale.showDialog();
		if (askToScale.wasOKed()) {
			GenericDialog scale = new GenericDialog("New Image");
			scale.addNumericField("pixels/micron?", 00.00, 4);
			scale.setCancelLabel("Get value in pixels");
			scale.showDialog();
			factor = scale.getNextNumber();
		}

		// set up progress bar
		double progress = 0.0;
		IJ.showProgress(progress);
		IJ.showMessage("Need to exit?", "Hold the the escape key to exit without saving anytime.");
		
		// analyze images
		RoiManager manager = new RoiManager();
		ArrayList<Double> lengths = new ArrayList<Double>();
		ArrayList<Integer> frames = new ArrayList<Integer>();
		int roiCount = 0;
		ImagePlus frame = null;
		
		// go through all frames in the movie and record the spindle length in each one
		for (int framenum = 1; framenum <= stack.getStackSize(); framenum++) {

			frame = IJ.openImage(imageName, framenum);
			//frame.show();
			System.out.println("Frame number: " + framenum);
			double length = -1.0; // the length will stay negative if the algorithm can't measure it
			try {
				progress = (double) framenum / (double) stack.getStackSize();
				length = getLength(frame, manager, progress);
//				System.out.println("Length: " + length);
				frames.add(framenum);
				lengths.add(length);
				manager.getRoi(roiCount).setPosition(framenum);
				roiCount++;
				manager.getRoi(roiCount).setPosition(framenum);
				roiCount++;
			
			} catch (Exception e) {
				System.out.println("Had trouble measuring frame " + framenum + " :(");
			}
			
			try {
				TimeUnit.SECONDS.sleep(1); // display each frame for 1 second
				if (IJ.escapePressed()) {
					System.exit(1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			IJ.showProgress(framenum, stack.getStackSize());
			frame.close(); // close image that is opened in the getLength() method
		}
		
		
		// scale lengths if necessary
		if (factor != 0) {
			for (int i = 0; i < lengths.size(); i++) {
				double old = lengths.get(i);
				lengths.set(i, old / factor);
			}
		}
		
		
		
		// write data to csv output file here
		File f = new File(filename);
		PrintStream out = null;
		try {
			out = new PrintStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		out.println("Frame number , x1, y1, x2, y2, length");
		roiCount = 0;

		for (int i = 0; i < frames.size(); i++) {
			out.println(frames.get(i) + "," + manager.getRoi(roiCount).getXBase() + "," +
					manager.getRoi(roiCount).getYBase() + "," + manager.getRoi(roiCount + 1).getXBase() + 
					"," + manager.getRoi(roiCount + 1).getYBase() + "," + lengths.get(i));
			roiCount += 2;
		
		}
		out.close();
		
		
		// build stack with ROIs overlaid on it
		Overlay displayList = new Overlay();
		
		for (int i = 0; i < manager.getCount(); i++) {
			displayList.add(manager.getRoi(i));
		}
		
		stack.setOverlay(displayList);

		IJ.runPlugIn(clazz.getName(), "");
		stack.show(); // this outputs the whole stack that you can scroll through
		
		// wait for people to update the ROIs if they need to
//		GenericDialog waitForROI= new GenericDialog("Scaling");
//		waitForROI.addMessage("Now's your chance to fix things");
//		waitForROI.hideCancelButton();
//		waitForROI.showDialog();
//		
				
	}
}
