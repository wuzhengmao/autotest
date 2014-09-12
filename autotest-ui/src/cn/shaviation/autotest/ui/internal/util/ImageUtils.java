package cn.shaviation.autotest.ui.internal.util;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public abstract class ImageUtils {

	public static final int TOP_LEFT = 0;
	public static final int TOP_RIGHT = 1;
	public static final int BOTTOM_LEFT = 2;
	public static final int BOTTOM_RIGHT = 3;
	public static final int UNDERLAY = 4;
	private static final PaletteData ALPHA_PALETTE;

	static {
		RGB[] rgbs = new RGB[256];
		for (int i = 0; i < rgbs.length; i++) {
			rgbs[i] = new RGB(i, i, i);
		}
		ALPHA_PALETTE = new PaletteData(rgbs);
	}

	private static final PaletteData BW_PALETTE = new PaletteData(new RGB[] {
			new RGB(0, 0, 0), new RGB(255, 255, 255) });

	private static int getTransparencyDepth(ImageData data) {
		if ((data.maskData != null) && (data.depth == 32)) {
			for (int i = 0; i < data.data.length; i += 4) {
				if (data.data[i] != 0) {
					return 8;
				}
			}
		}
		if ((data.maskData != null) || (data.transparentPixel != -1)) {
			return 1;
		}
		if ((data.alpha != -1) || (data.alphaData != null)) {
			return 8;
		}
		return 0;
	}

	private static ImageData getTransparency(ImageData data,
			int transparencyDepth) {
		if (data == null) {
			return null;
		}
		if (transparencyDepth == 1) {
			return data.getTransparencyMask();
		}
		ImageData mask = null;
		if ((data.maskData != null) && (data.depth == 32)) {
			ImageData m = data.getTransparencyMask();
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int alpha = data.getPixel(x, y) & 0xFF;
					if ((alpha == 0) && (m.getPixel(x, y) != 0)) {
						alpha = 255;
					}
					mask.setPixel(x, y, alpha);
				}
			}
		} else if ((data.maskData != null) || (data.transparentPixel != -1)) {
			ImageData m = data.getTransparencyMask();
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int y = 0; y < mask.height; y++) {
				for (int x = 0; x < mask.width; x++) {
					mask.setPixel(x, y, m.getPixel(x, y) != 0 ? -1 : 0);
				}
			}
		} else if (data.alpha != -1) {
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int i = 0; i < mask.data.length; i++) {
				mask.data[i] = ((byte) data.alpha);
			}
		} else if (data.alphaData != null) {
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, data.alphaData);
		} else {
			mask = new ImageData(data.width, data.height, 8, ALPHA_PALETTE,
					data.width, new byte[data.width * data.height]);
			for (int i = 0; i < mask.data.length; i++) {
				mask.data[i] = -1;
			}
		}
		return mask;
	}

	private static void composite(ImageData dst, ImageData src, int xOffset,
			int yOffset) {
		if (dst.depth == 1) {
			int y = 0;
			for (int dstY = y + yOffset; y < src.height; dstY++) {
				int x = 0;
				for (int dstX = x + xOffset; x < src.width; dstX++) {
					if ((dstX >= 0) && (dstX < dst.width) && (dstY >= 0)
							&& (dstY < dst.height) && (src.getPixel(x, y) != 0)) {
						dst.setPixel(dstX, dstY, 1);
					}
					x++;
				}
				y++;
			}
		} else if (dst.depth == 8) {
			int y = 0;
			for (int dstY = y + yOffset; y < src.height; dstY++) {
				int x = 0;
				for (int dstX = x + xOffset; x < src.width; dstX++) {
					if ((dstX >= 0) && (dstX < dst.width) && (dstY >= 0)
							&& (dstY < dst.height)) {
						int srcAlpha = src.getPixel(x, y);
						int dstAlpha = dst.getPixel(dstX, dstY);
						dstAlpha += (srcAlpha - dstAlpha) * srcAlpha / 255;
						dst.setPixel(dstX, dstY, dstAlpha);
					}
					x++;
				}
				y++;
			}
		}
	}

	private static Image compositeImage(Device device, ImageData base,
			ImageData[] overlay) {
		if (base == null) {
			return null;
		}
		Image image = new Image(device, new ImageData(base.width, base.height,
				24, new PaletteData(255, 65280, 267386880)));
		GC gc = new GC(image);

		int maskDepth = 0;
		int baseMaskDepth = 0;
		ImageData src;
		ImageData underlay = src = overlay.length > UNDERLAY ? overlay[UNDERLAY]
				: null;
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			Image img = new Image(device, src);
			gc.drawImage(img, 0, 0);
			img.dispose();
		}
		src = base;

		maskDepth = Math.max(maskDepth,
				baseMaskDepth = getTransparencyDepth(src));
		Image img = new Image(device, src);
		gc.drawImage(img, 0, 0);
		img.dispose();

		ImageData topLeft = src = overlay[TOP_LEFT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			img = new Image(device, src);
			gc.drawImage(img, 0, 0);
			img.dispose();
		}
		ImageData topRight = src = overlay[TOP_RIGHT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			img = new Image(device, src);
			gc.drawImage(img, base.width - src.width, 0);
			img.dispose();
		}
		ImageData bottomLeft = src = overlay[BOTTOM_LEFT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			img = new Image(device, src);
			gc.drawImage(img, 0, base.height - src.height);
			img.dispose();
		}
		ImageData bottomRight = src = overlay[BOTTOM_RIGHT];
		if (src != null) {
			maskDepth = Math.max(maskDepth, getTransparencyDepth(src));
			img = new Image(device, src);
			gc.drawImage(img, base.width - src.width, base.height - src.height);
			img.dispose();
		}
		gc.dispose();
		if (baseMaskDepth > 0) {
			ImageData newData = image.getImageData();
			image.dispose();
			ImageData mask = null;
			switch (maskDepth) {
			case 1:
				mask = new ImageData(base.width, base.height, maskDepth,
						BW_PALETTE);
				break;
			case 8:
				mask = new ImageData(base.width, base.height, maskDepth,
						ALPHA_PALETTE, base.width, new byte[base.width
								* base.height]);
			}
			src = getTransparency(underlay, maskDepth);
			if (src != null) {
				composite(mask, src, 0, 0);
			}
			src = getTransparency(base, maskDepth);
			if (src != null) {
				composite(mask, src, 0, 0);
			}
			src = getTransparency(topLeft, maskDepth);
			if (src != null) {
				composite(mask, src, 0, 0);
			}
			src = getTransparency(topRight, maskDepth);
			if (src != null) {
				composite(mask, src, mask.width - src.width, 0);
			}
			src = getTransparency(bottomLeft, maskDepth);
			if (src != null) {
				composite(mask, src, 0, mask.height - src.height);
			}
			src = getTransparency(bottomRight, maskDepth);
			if (src != null) {
				composite(mask, src, mask.width - src.width, mask.height
						- src.height);
			}
			switch (maskDepth) {
			case 1:
				newData.maskData = mask.data;
				newData.maskPad = mask.scanlinePad;
				break;
			case 8:
				newData.alphaData = mask.data;
			}
			image = new Image(device, newData);
		}
		return image;
	}

	public static Image compositeImage(Image base, Image overlay, int position) {
		ImageData[] overlays = new ImageData[5];
		overlays[position] = overlay.getImageData();
		return compositeImage(base.getDevice(), base.getImageData(), overlays);
	}
}
