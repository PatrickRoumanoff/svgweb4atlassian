package com.roumanoff.svgweb4a;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderException;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.importexport.resource.DownloadResourceWriter;
import com.atlassian.confluence.importexport.resource.ExportDownloadResourceManager;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.renderer.PageContext;
import com.atlassian.confluence.renderer.radeox.macros.MacroUtils;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.util.velocity.VelocityUtils;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.User;

public class SvgWeb4a extends BaseMacro {

	private static final String ATTACHMENT_TEMPLATE = "templates/attachment.vm";
	private static final String INLINE_TEMPLATE = "templates/inline.vm";

	public static final String FILE_NAME = "file";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";

	private final SettingsManager settingsManager;
	private final ExportDownloadResourceManager exportDownloadResourceManager;
	private final AttachmentManager attachmentManager;
	
	public SvgWeb4a(SettingsManager settingsManager, AttachmentManager attachmentManager) {
		this.settingsManager = settingsManager;
		this.attachmentManager = attachmentManager;
		//For whatever reasons this can't be wired up by spring based on type alone (at least using sdk 3.0 beta 5)
		this.exportDownloadResourceManager = (ExportDownloadResourceManager) ContainerManager.getComponent("exportDownloadResourceManager");
	}

	public boolean isInline() {
		return false;
	}

	public boolean hasBody() {
		return true; // in case of inline svg content
	}

	public RenderMode getBodyRenderMode() {
		return RenderMode.NO_RENDER; // we want raw content (supposed to be valid xml)
	}

	@SuppressWarnings("unchecked")
	public String execute(Map parameters, String body, RenderContext renderContext) throws MacroException {
		PageContext pageContext = (PageContext) renderContext;

		Map<String, Object> context = MacroUtils.defaultVelocityContext();

		// Only use svgweb for PREVIEW and DISPLAY use PNG export for all other types
		boolean svgEnabled = false;

		if (RenderContext.PREVIEW.equals(renderContext.getOutputType())) {
			svgEnabled = true;
		} else if (RenderContext.DISPLAY.equals(renderContext.getOutputType())) {
			svgEnabled = true;
		}

		// if (!(renderContext instanceof PageContext)) {
		// throw new MacroException("This macro can only be used in a page");
		// }
		// ContentEntityObject contentObject = ((PageContext) renderContext).getEntity();

		String width = (String) parameters.get(WIDTH);
		if (width == null) {
			width = "500";
		}

		String heigth = (String) parameters.get(HEIGHT);
		if (heigth == null) {
			heigth = "500";
		}

		context.put("id", "id" + System.currentTimeMillis()); //a random unique id
		context.put("body", body);
		context.put("width", width);
		context.put("height", heigth);

		String link = (String) parameters.get(FILE_NAME);
		if (link == null) {
			link = (String) parameters.get("0");
		}
		if (svgEnabled) {
			return svg(link, body, width, heigth, pageContext.getEntity(), context);
		} else {
			return png(link, body, width, heigth, pageContext.getEntity());
		}
	}

	public String svg(String link, String body, String width, String height,  ContentEntityObject contentEntityObject, Map<String, Object> context)
			throws MacroException {
		String baseUrl = settingsManager.getGlobalSettings().getBaseUrl();
		if (link != null) {
			Attachment attachment = attachmentManager.getAttachment(contentEntityObject, link);
			if (attachment != null) {
				context.put("urlPath", baseUrl + attachment.getDownloadPath());
				return VelocityUtils.getRenderedTemplate(ATTACHMENT_TEMPLATE, context);
			}
			throw new MacroException("can't find attachment:" + link);
		} else if (body != null) {
			return VelocityUtils.getRenderedTemplate(INLINE_TEMPLATE, context);
		} else {
			throw new MacroException("Either file parameter or inline content must be specified for svgweb4a macro");
		}
	}

	public String png(String link, String body, String width, String height, ContentEntityObject contentEntityObject)
			throws MacroException {
		// get the current user
		User user = AuthenticatedUserThreadLocal.getUser();
		String userName = user == null ? "" : user.getName();

		// get the resource writer
		DownloadResourceWriter writer = exportDownloadResourceManager.getResourceWriter(userName, "svgweb4a", ".png");
		OutputStream outputStream = writer.getStreamForWriting();

		try {
			if (link != null) {
				Attachment attachment = attachmentManager.getAttachment(contentEntityObject, link);
				if (attachment != null) {
					SVGHelper.convertSVGToPNG(attachment, width, height, outputStream);
					return "<img src=\"" + writer.getResourcePath() + "\">";
				}
				throw new MacroException("can't find attachment:" + link);
			} else if (body != null) {
				SVGHelper.convertSVGToPNG("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + body.trim(), width, height, outputStream);
				return "<img src=\"" + writer.getResourcePath() + "\">";
			} else {
				throw new MacroException("Either file parameter or inline content must be specified for svgweb4a macro");
			}
		} catch (IOException e) {
			throw new MacroException(e);
		} catch (TranscoderException e) {
			throw new MacroException(e);
		}
	}
}