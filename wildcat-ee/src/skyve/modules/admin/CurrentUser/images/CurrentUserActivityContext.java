package modules.admin.CurrentUser.images;

import java.awt.image.BufferedImage;

import modules.admin.Display.images.ActivityContext;
import modules.admin.domain.CurrentUser;

import org.skyve.metadata.model.document.DynamicImage;
import org.skyve.metadata.user.User;

public class CurrentUserActivityContext implements DynamicImage<CurrentUser> {
	/**
	 * For Serialization
	 */
	private static final long serialVersionUID = 920018115413956116L;

	/**
	 * Construct a line chart of hits for this user over the last 12 months
	 */
	@Override
	public BufferedImage getImage(CurrentUser bean, int width, int height, User user) throws Exception {
		
		if (bean != null && bean.getCurrentUser() != null) {
			return ActivityContext.getActivityContextPieImage(bean.getCurrentUser(), width, height, user);
		}
		return null;
	}

	@Override
	public ImageFormat getFormat() {
		return null;
	}

	@Override
	public Float getCompressionQuality() {
		return null;
	}
}