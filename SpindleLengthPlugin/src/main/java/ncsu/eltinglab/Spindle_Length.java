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
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.NewImage;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;

/**
 * Measures spindle length in each frame of a TIFF stack
 * adapted from https://github.com/imagej/example-legacy-plugin
 *
 * @author Johannes Schindelin
 * @author Ana Sofia Uzsoy
 */
public class Spindle_Length implements PlugInFilter {
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

		// Plugin runs on current open image
		ImageStack stack = IJ.getImage().getImageStack();
//		
//		String imageName = "input/Stack-1.tif";
//		ImagePlus stack2 = IJ.openImage(imageName);
//	    ImageStack stack = stack2.getImageStack();
//	    IJ.openImage(imageName);
	
		//System.out.println("Running");
		
		// prompts user for output filename
		IJ.showMessage("Choose where to save output file, called lengths.csv");

		String folder = IJ.getDirectory("Select folder: ");
		String filename = folder + "lengths.csv";

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
		//RoiManager manager = new RoiManager();
		RoiManager manager = RoiManager.getInstance();
		if (manager == null) {
			manager = new RoiManager();
		}
		ArrayList<Double> lengths = new ArrayList<Double>();
		ArrayList<Integer> frames = new ArrayList<Integer>();
		int roiCount = 0;
		ImagePlus frame = null;
		
		// go through all frames in the movie and record the spindle length in each one
		for (int framenum = 1; framenum <= stack.getSize(); framenum++) {

			frame = new ImagePlus("Frame " + framenum, stack.getProcessor(framenum));
			//frame.show();
			double length = -1.0; // the length will stay negative if the algorithm can't measure it
			try {
				progress = (double) framenum / (double) stack.getSize();
				length = getLength(frame, manager, progress);
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
				//TimeUnit.SECONDS.sleep(1); // display each frame for 1 second
				TimeUnit.MILLISECONDS.sleep(500);
				if (IJ.escapePressed()) {
					System.exit(1);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			IJ.showProgress(framenum, stack.getSize());
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

		out.println("Frame number , x1, y1, x2, y2, length"); //header line
		roiCount = 0;

		for (int i = 0; i < frames.size(); i++) {
			out.println(frames.get(i) + "," + manager.getRoi(roiCount).getXBase() + "," +
					manager.getRoi(roiCount).getYBase() + "," + manager.getRoi(roiCount + 1).getXBase() + 
					"," + manager.getRoi(roiCount + 1).getYBase() + "," + lengths.get(i));
			System.out.println("Length: " + lengths.get(i));
			roiCount += 2;
		
		}
		out.close();
		
		
		// build stack with ROIs overlaid on it
		Overlay displayList = new Overlay();
		displayList.drawLabels(false);
		
		for (int i = 0; i < manager.getCount(); i++) {
			displayList.add(manager.getRoi(i));
		}
		
		ImagePlus back = new ImagePlus("Full Stack", stack);
		
		back.setOverlay(displayList);
		back.show();
		
	}

	private boolean showDialog() { // to hijack later for pop-up menu, etc
		GenericDialog gd = new GenericDialog("Process pixels");

		// default value is 0.00, 2 digits right of the decimal point
		gd.addNumericField("value", 0.00, 2);
		gd.addStringField("name", "John");

		gd.showDialog();
		if (gd.wasCanceled())
			return false;

		// get entered values
		value = gd.getNextNumber();
		name = gd.getNextString();
		
		
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
	
	public static int[] old_to_new(ImageProcessor proc, double old_x, double old_y, double xvector, double yvector) {
		double angle = Math.atan(yvector / xvector) * (180 / Math.PI); // angle from positive x axis
		double rotation_angle = (270 + angle) * (Math.PI/180); // actual angle to rotate by (converted to radians)
		
		
		double cos = Math.cos(rotation_angle);
		double sin = Math.sin(rotation_angle);
		
		int height = proc.getHeight();
		int width = proc.getWidth();
		
//		int new_height = ((int) (Math.abs(height * cos) + Math.abs(width * sin))) + 1;
//		int new_width = ((int) (Math.abs(width * cos) + Math.abs(height * sin))) + 1;
		
		int new_height = proc.getHeight();
		int new_width = proc.getWidth();
				
		int new_center_x = (int) ((new_width + 1) / 2 - 1);
		int new_center_y = (int) ((new_height + 1) / 2 - 1); 
		
		double x = width - 1 - old_x - (int) ((width + 1)/2 - 1);
		double y = height - 1 - old_y - (int) ((height + 1)/2 - 1);
		
		double new_x = cos * x + sin * y;
		double new_y = -sin * x + cos*y;
		
		int new_x_val = new_center_x - (int) new_x;
		int new_y_val = new_center_y - (int) new_y;
		
		int[] new_vals = {new_x_val, new_y_val};
		
		return new_vals;
	}
	
	public static ImageProcessor rotate(ImageProcessor proc, double xvector, double yvector) {
				
		double angle = Math.atan(yvector / xvector) * (180 / Math.PI); // angle from positive x axis
		double rotation_angle = (270 + angle) * (Math.PI/180); // actual angle to rotate by (converted to radians)
		
		int mode = proc.getStatistics().mode;
		System.out.println(mode);
		
		double cos = Math.cos(rotation_angle);
		double sin = Math.sin(rotation_angle);
		
		int height = proc.getHeight();
		int width = proc.getWidth();
		
		int new_height = ((int) (Math.abs(height * cos) + Math.abs(width * sin))) + 1;
		int new_width = ((int) (Math.abs(width * cos) + Math.abs(height * sin))) + 1;
		
		ImageProcessor rot = proc.createProcessor(new_width, new_height);
		
		int new_center_x = (int) ((new_width + 1) / 2 - 1);
		int new_center_y = (int) ((new_height + 1) / 2 - 1);

		for (int i = 0; i < proc.getHeight(); i++) {
			for (int j = 0; j < proc.getWidth(); j++) {
				double y = height - 1 - i - (int) ((height + 1)/2 - 1);
				double x = width - 1 - j - (int) ((width + 1)/2 - 1);
				double new_x = cos * x + sin * y;
				double new_y = -sin * x + cos*y;
				rot.set(new_center_x - (int) new_x, new_center_y - (int) new_y, proc.get(j, i));
				
			}
		}
		
		for (int i = 0; i < rot.getHeight(); i++) {
			for (int j = 0; j < rot.getWidth(); j++) {
				if (rot.get(j, i) == 0) {
					rot.set(j, i, mode * (65535/256));
				}
			}
		}

		
		return rot;
	}
	
	public static double getAngle(double xvector, double yvector) {
		double angle = Math.atan(yvector / xvector) * (180 / Math.PI); // angle from positive x axis
		double rotation_angle = (90 - angle); // actual angle to rotate by (in degrees)
		
		return rotation_angle;
	}
	
	public static ImageProcessor[] cropRotatedImage(ImageProcessor original, ImageProcessor edited, double xcm) {
		
		//ImagePlus new_im = new ImagePlus();
		
		int top = original.getHeight() - 1;
		int bottom = 0;
		
		while (original.get((int) xcm, top) == 0) {
			top--;
		}
		
		while (original.get((int) xcm, bottom) == 0) {
			bottom++;
		}
		
		ImagePlus new_im_orig = NewImage.createImage("cropped", original.getWidth(), top - bottom + 1, 1, 16, 6);
		ImagePlus new_im_edit = NewImage.createImage("cropped_edit", edited.getWidth(), top - bottom + 1, 1, 16, 6);
		ImageProcessor new_proc = new_im_orig.getProcessor();
		ImageProcessor new_proc_edit = new_im_edit.getProcessor();
		
		System.out.println("Top, bottom = " + top + " , " + bottom);
		
		
		for (int i = 0; i < original.getWidth(); i++) {
			for (int j = 0; j < (top - bottom); j++)  {
				new_proc.set(i, j, original.get(i, bottom + j));
				new_proc_edit.set(i, j, edited.get(i, bottom + j));
			}
		
		}
		
		ImageProcessor[] proc_arr = {new_proc, new_proc_edit};
	
		
		return proc_arr;
	}

	public static double getLength(ImagePlus im, RoiManager m, double progress) throws Exception{
		
		ImagePlus im2 = im.duplicate();
		ImageProcessor edited = im2.getProcessor();

	    System.err.println("Running");	
		// normalize all images (with linear transformation) so they go from 0 to 65535
		double maximum = Integer.MIN_VALUE;
		double minimum = Integer.MAX_VALUE;
				
		for (int i = 0; i < edited.getWidth(); i++) {
			for (int j = 0; j < edited.getHeight(); j++) {
				if (edited.get(i,j) > maximum) {
					maximum = edited.get(i, j);
				}
				if (edited.get(i,j) < minimum) {
					minimum = edited.get(i, j);
				}
				
			}
		}
		
		//System.out.println("Maximum: " + maximum + " Minimum: " + minimum);
		
		double slope = 65535.0 / (maximum - minimum);
		double intercept = -1 * slope * minimum;
		
		//System.out.println("Slope: " + slope);
		//System.out.println("Intercept: " + intercept);
		
		
		for (int i = 0; i < edited.getWidth(); i++) {
			for (int j = 0; j < edited.getHeight(); j++) {
				int old = edited.get(i,j);
				double n = slope * old + intercept;	
				edited.set(i, j, (int) n);
				edited.putPixel(i, j, (int) n);
				//System.out.println(old + " , " + proc.get(i,j));
			}
		}		
		
		maximum = Integer.MIN_VALUE;
		minimum = Integer.MAX_VALUE;
		
		for (int i = 0; i < edited.getWidth(); i++) {
			for (int j = 0; j < edited.getHeight(); j++) {
				if (edited.get(i,j) > maximum) {
					maximum = edited.get(i, j);
				}
				if (edited.get(i,j) < minimum) {
					minimum = edited.get(i, j);
				}
				
			}
		}
		
		
		ImageProcessor original = edited.duplicate(); // keep a copy of the original for later
		
		// Making a giant comma-separated list of pixel values to pass into the 
		// Python script as a fake command line argument
		StringBuilder pixelString = new StringBuilder("");
		for (int i = 0; i < edited.getWidth(); i++) {
			for (int j = 0; j < edited.getHeight(); j++) {
				pixelString.append(edited.get(i, j) + ",");
			}
		}
		pixelString.deleteCharAt(pixelString.length() - 1); //removes trailing comma
	
		//System.out.println("Writing pixels to file...");
		File f = new File("python/pixelstring.txt");
		if (f.exists()) {
			f.delete();
		}
		try {
			f.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		PrintStream pixels = null;
		try {
			pixels = new PrintStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	
		pixels.println(pixelString);
		pixels.close();
		
		//System.out.println("Wrote pixels to file!");
		
		// runs python script to interpolate a threshold based on the pixel array
		String s1 = null;
		String s2 = null;
		double thresh = 0.0;
		try {
			Process p;
			if (IJ.isWindows()) {
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "python \"python/newinterpolation.py\" ");
		        p = builder.start();
			} else {
				p = Runtime.getRuntime().exec("python python/newinterpolation.py " );
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((s1 = stdInput.readLine()) != null) {
                //System.out.println(s1);
				s2 = s1;
            }
            p.destroy();
            
            //System.out.println(s2);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		thresh = Double.valueOf(s2);
		
		//System.out.println("Threshold: " + thresh);
		
		//System.out.println("Successfully read file!");
		
		
		
		// sets all pixels below the threshold to zero intensity
		for (int i = 0; i < edited.getWidth(); i++) {
			for (int j = 0; j < edited.getHeight(); j++) {
				if (edited.get(i, j) < thresh) {
					edited.set(i, j, 0);
				} 
			}
		}
		
		// checks for isolated random bright pixels and sets them to zero
		// we only do this in the first 80% because it's too difficult on fainter frames
		if (progress < 0.8) {
			for (int i = 0; i < edited.getWidth() - 1; i++) {
				for (int j = 0; j < edited.getHeight() - 1; j++) {
					if (edited.get(i, j) != 0 ) {
						if (edited.get(i, j + 1) == 0 && edited.get(i, j - 1) == 0) {
							if (edited.get(i + 1, j) == 0 && edited.get(i - 1, j) == 0) {
								edited.set(i, j, 0);
							}
						}
					}
					
				}
			}
		}
				
		// calculates center of mass coordinates
		double xsum = 0;
		double ysum = 0;
		double mass = 0.0;
		for (int i = 1; i < edited.getWidth(); i++) {
			for (int j = 1; j < edited.getHeight(); j++) {
				mass += edited.get(i, j);
				xsum += edited.get(i, j) * i;
				ysum += edited.get(i, j) * j;
			}
		}
		
		double xcm = xsum/mass;
		double ycm = ysum/mass;
		
		//System.out.println("xcm: " + xcm + " ycm: " + ycm);

		// calculates moment of inertia tensor "matrix"
		double Ixx = 0;
		double Iyy = 0;
		double Ixy = 0;
		for (int i = 0; i < edited.getWidth(); i++) {
			for (int j = 0; j < edited.getHeight(); j++) {
				Ixx = Ixx + edited.get(i, j) * (i - xcm) * (i - xcm);
				Iyy = Iyy + edited.get(i, j) * (j - ycm) * (j - ycm);
				Ixy = Ixy + edited.get(i, j) * (i - xcm) * (j - ycm);
			}
		}
		
		String matrixString = Ixx + "," + Ixy + "," + Ixy + "," + Iyy;

		File f1 = new File("python/matrixstring.txt");
		if (f1.exists()) {
			f1.delete();
		}
		try {
			f1.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		
		PrintStream matrix = null;
		try {
			matrix = new PrintStream(f1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	
		matrix.println(matrixString);
		matrix.close();
		
		
		// passes in matrix string to python script which outputs principal eigenvectors
		double xvector = 1.0;
		double yvector = 1.0;
		
		double minorx = 1.0;
		double minory = 1.0;
		try {
			Process p;
			if (IJ.isWindows()) {
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "python \"python/lazylinalg.py\" ");
		        p = builder.start();
			} else {
				p = Runtime.getRuntime().exec("python python/lazylinalg.py ");
			}
		    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            xvector = Double.valueOf(stdInput.readLine());
            yvector = Double.valueOf(stdInput.readLine());
            minorx = Double.valueOf(stdInput.readLine());
            minorx = Double.valueOf(stdInput.readLine());
            p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// this is where we rotate the image
//		
//		System.out.println("xvector: " + xvector);
//		System.out.println("yvector: " + yvector);
//		System.out.println("xcm:" + xcm);
//		System.out.println("ycm:" + ycm);
	
		
		double rot_angle = getAngle(xvector, yvector);
		
		edited.rotate(rot_angle);
		original.rotate(rot_angle);
		
		System.out.println("Rotation angle: " + rot_angle);
		
		int[] new_cms = old_to_new(edited, xcm, ycm, xvector, yvector);
		
		System.out.println("center of mass:" + new_cms[0] + " " + new_cms[1]);
		
		// need to crop the original and edited one the same way based on the original
		ImageProcessor[] cropped_procs = cropRotatedImage(original, edited, new_cms[0]);
		ImageProcessor cropped_original = cropped_procs[0];
		ImageProcessor cropped_edited = cropped_procs[1];
		ImagePlus cropped = new ImagePlus("cropped", cropped_original);
		cropped.show();

		edited = cropped_edited;
		original = cropped_original;
				
		yvector = 1.0;
		xvector = 0.0; // now that we've rotated the image for the spindle to be vertical
		
		minorx = 1.0;
		minory = 0.0;
		
		// backtrack vector from center of mass to edge of image
		double x = new_cms[0];
		double y = new_cms[1];
		while ((x > 0 && y > 0) && (x < (edited.getWidth() - Math.abs(xvector)) && y < (edited.getHeight() - Math.abs(yvector)))) {
			x += xvector;
			y += yvector;
		}

//		
//		System.out.println(x);
//		System.out.println(y);
		
		
		ArrayList<Integer> intensities = new ArrayList<Integer>();
		ArrayList<Integer> intensities_integrate = new ArrayList<Integer>();
		ArrayList<Integer> indexes = new ArrayList<Integer>();
		ArrayList<Double> pointsx = new ArrayList<Double>();
		ArrayList<Double> pointsy = new ArrayList<Double>();
		
		// iterates over pixels in line through spindle across the frame, integrates over a 
		// width of two spindles, adding intensities to above arrays
		int index = 0;
		int pivot = 0; // going to be the index of the center of mass
		do {
			x += -1 * xvector;
			y += -1 * yvector;

//			System.out.println(x);
//			System.out.println(y);
//			
			
			intensities.add(edited.get((int) x,(int) y));

			int intensity = original.get((int) x,(int) y) / 1000;
			if ((x + 2 * minorx < original.getWidth() && x + 2 * minorx >= 0) && (x - 2 * minorx < original.getWidth() && x - 2 * minorx >= 0)) {
				if ((y + 2 * minory < original.getHeight() && y + 2 * minory >= 0) && (y - 2 * minory < original.getHeight() && y - 2 * minory >= 0)) {
					intensity += (original.get((int)(x + minorx), (int) (y + minory)) / 1000);
					intensity += (original.get((int)(x - minorx), (int) (y - minory)) / 1000);	
					intensity += (original.get((int)(x + 2 * minorx), (int) (y + 2 * minory)) / 1000);
					intensity += (original.get((int)(x - 2 * minorx), (int) (y - 2 * minory)) / 1000);
					
					
					// original.set((int) x, (int) y, 65534); uncomment this and comments underneath to see the line it draws
				}
			}
			

			
			intensities_integrate.add(intensity);
			if ((int) x == (int) xcm) {
				pivot = index;
			}

			indexes.add(index);
			index++;
			pointsx.add(x);
			pointsy.add(y);
			//System.out.println(index);
		} while ((x > ( Math.abs(xvector)) && y > (Math.abs(yvector))) && 
				(x < (original.getWidth() - Math.abs(xvector)) && y < (original.getHeight() - Math.abs(yvector))));

//		ImagePlus show_line = new ImagePlus("cropped", original);
//		show_line.show();
		
		
		//System.out.println("here");
		
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

		
		
		
		
//		System.out.println(minindex);
//		System.out.println(maxindex);
		
		// runs curve fitting code
		StringBuilder intenseString = new StringBuilder("");
		StringBuilder indexString = new StringBuilder("");
		for (int i = 0; i < intensities.size(); i++) {
			intenseString.append(intensities_integrate.get(i) + ",");
			indexString.append(indexes.get(i) + ",");
		}
		
		intenseString.deleteCharAt(intenseString.length() - 1);
		indexString.deleteCharAt(indexString.length() - 1); // removes trailing comma
		
	
		File f2 = new File("python/intensitiesstring.txt");
		
		if (f2.exists()) {
			f2.delete();
		}
		try {
			f2.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		PrintStream intense = null;
		try {
			intense = new PrintStream(f2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	
		intense.println(indexString);
		intense.println(intenseString);
		intense.println(pivot);
		intense.close();
		
		
		
		double rsquared = 0.0;
		double l = -1.0;
		int oneend = 0;
		int otherend = 0;
		try {
			Process p;
			if (IJ.isWindows()) {
				ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "python \"python/curvefit2.py\" ");
		        p = builder.start();
			} else {
				p = Runtime.getRuntime().exec("python python/curvefit2.py ");
			}
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
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


		// calculate spindle length
        // uses curve fit length only if r squared is greater than 0.75
		double deltax = Math.abs(pointsx.get(minindex) - pointsx.get(maxindex));
		double deltay = Math.abs(pointsy.get(minindex) - pointsy.get(maxindex));

		double length = Math.sqrt(deltax * deltax + deltay * deltay);
		
		double max_length = Math.sqrt(original.getHeight() * original.getHeight() + original.getWidth() * original.getWidth());

		System.out.println("R^2: " + rsquared);

		
		if (rsquared >= 0.85 && l < max_length) {
			minindex = oneend;
			maxindex = otherend;
			length = l;
		} else {
			System.out.println("Curve fit was bad!");
		}
		
		//new ImagePlus("image", proc).show();
		
		// add ROI circles on the ends of the spindle and add them to the ROI manager.
		Vector<Roi> displayList = new Vector<Roi>();
		Roi circle = new OvalRoi(pointsx.get(minindex), pointsy.get(minindex), 5, 5);
		circle.setFillColor(Color.MAGENTA);
		Roi circle2 = new OvalRoi(pointsx.get(maxindex), pointsy.get(maxindex), 5, 5);
		circle2.setFillColor(Color.MAGENTA);
		displayList.add(circle);
		displayList.add(circle2);		
		
//		ImagePlus new_im = new ImagePlus("rotated", proc2);	
//		new_im.show();
		ImageCanvas c = cropped.getCanvas();
		//ImageCanvas c = im.getCanvas();
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
		Class<?> clazz = Spindle_Length.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring("file:".length(), url.length() - clazz.getName().length() - ".class".length());
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// open the image
		String imageName = "input/Cell1.tif";
		ImagePlus stack = IJ.openImage(imageName);
		System.out.println("Stack size: " + stack.getStackSize());
		
		
		
		ImagePlus frame = IJ.openImage(imageName, 85);
		frame.show();
		
		
		// run plugin
		IJ.runPlugIn(clazz.getName(), "");	
	}
}
