package fr.sii.ogham.html.inliner.impl.jsoup;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import fr.sii.ogham.core.util.Base64Utils;
import fr.sii.ogham.email.attachment.Attachment;
import fr.sii.ogham.html.inliner.ContentWithImages;
import fr.sii.ogham.html.inliner.ImageInliner;
import fr.sii.ogham.html.inliner.ImageResource;

/**
 * Image inliner that reads the image and converts it into a base64 string. The
 * string is then included directly in the HTML content using the src attribute
 * of img tag.
 * <p>
 * If the <code>img</code> tag has the attribute skip-base64, then the image is
 * not converted in base64 and not inlined.
 * </p>
 * 
 * @author Aurélien Baudet
 *
 */
public class JsoupBase64ImageInliner implements ImageInliner {
	private static final String SRC_ATTR = "src";
	private static final String IMG_SELECTOR = "img[src=\"{0}\"]";
	private static final String BASE64_URI = "data:{0};base64,{1}";
	private static final String BASE64_MODE = "base64";

	@Override
	public ContentWithImages inline(String htmlContent, List<ImageResource> images) {
		Document doc = Jsoup.parse(htmlContent);
		for (ImageResource image : images) {
			Elements imgs = getImagesToInline(doc, image);
			for (Element img : imgs) {
				img.attr(SRC_ATTR, MessageFormat.format(BASE64_URI, image.getMimetype(), Base64Utils.encodeToString(image.getContent())));
			}
		}
		return new ContentWithImages(doc.outerHtml(), new ArrayList<Attachment>(0));
	}

	private Elements getImagesToInline(Document doc, ImageResource image) {
		Elements imgs = doc.select(MessageFormat.format(IMG_SELECTOR, image.getPath()));
		Elements found = new Elements();
		for (Element img : imgs) {
			// skip images that have skip-base64 attribute
			if (JsoupUtils.isInlineModeAllowed(img, BASE64_MODE)) {
				found.add(img);
			}
		}
		return found;
	}
}