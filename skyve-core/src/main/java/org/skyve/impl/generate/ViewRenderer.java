package org.skyve.impl.generate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

import org.skyve.CORE;
import org.skyve.domain.Bean;
import org.skyve.impl.bind.BindUtil;
import org.skyve.impl.metadata.Container;
import org.skyve.impl.metadata.customer.CustomerImpl;
import org.skyve.impl.metadata.model.document.DocumentImpl;
import org.skyve.impl.metadata.model.document.field.Text;
import org.skyve.impl.metadata.module.ModuleImpl;
import org.skyve.impl.metadata.view.ActionImpl;
import org.skyve.impl.metadata.view.HorizontalAlignment;
import org.skyve.impl.metadata.view.Inject;
import org.skyve.impl.metadata.view.ViewImpl;
import org.skyve.impl.metadata.view.ViewVisitor;
import org.skyve.impl.metadata.view.container.HBox;
import org.skyve.impl.metadata.view.container.Tab;
import org.skyve.impl.metadata.view.container.TabPane;
import org.skyve.impl.metadata.view.container.VBox;
import org.skyve.impl.metadata.view.container.form.Form;
import org.skyve.impl.metadata.view.container.form.FormColumn;
import org.skyve.impl.metadata.view.container.form.FormItem;
import org.skyve.impl.metadata.view.container.form.FormRow;
import org.skyve.impl.metadata.view.event.ServerSideActionEventAction;
import org.skyve.impl.metadata.view.widget.Blurb;
import org.skyve.impl.metadata.view.widget.Button;
import org.skyve.impl.metadata.view.widget.DialogButton;
import org.skyve.impl.metadata.view.widget.DynamicImage;
import org.skyve.impl.metadata.view.widget.GeoLocator;
import org.skyve.impl.metadata.view.widget.Link;
import org.skyve.impl.metadata.view.widget.MapDisplay;
import org.skyve.impl.metadata.view.widget.Spacer;
import org.skyve.impl.metadata.view.widget.StaticImage;
import org.skyve.impl.metadata.view.widget.bound.Label;
import org.skyve.impl.metadata.view.widget.bound.ProgressBar;
import org.skyve.impl.metadata.view.widget.bound.input.CheckBox;
import org.skyve.impl.metadata.view.widget.bound.input.CheckMembership;
import org.skyve.impl.metadata.view.widget.bound.input.ColourPicker;
import org.skyve.impl.metadata.view.widget.bound.input.Combo;
import org.skyve.impl.metadata.view.widget.bound.input.Comparison;
import org.skyve.impl.metadata.view.widget.bound.input.ContentImage;
import org.skyve.impl.metadata.view.widget.bound.input.ContentLink;
import org.skyve.impl.metadata.view.widget.bound.input.Geometry;
import org.skyve.impl.metadata.view.widget.bound.input.HTML;
import org.skyve.impl.metadata.view.widget.bound.input.ListMembership;
import org.skyve.impl.metadata.view.widget.bound.input.Lookup;
import org.skyve.impl.metadata.view.widget.bound.input.LookupDescription;
import org.skyve.impl.metadata.view.widget.bound.input.Password;
import org.skyve.impl.metadata.view.widget.bound.input.Radio;
import org.skyve.impl.metadata.view.widget.bound.input.RichText;
import org.skyve.impl.metadata.view.widget.bound.input.Slider;
import org.skyve.impl.metadata.view.widget.bound.input.Spinner;
import org.skyve.impl.metadata.view.widget.bound.input.TextArea;
import org.skyve.impl.metadata.view.widget.bound.input.TextField;
import org.skyve.impl.metadata.view.widget.bound.tabular.AbstractDataWidget;
import org.skyve.impl.metadata.view.widget.bound.tabular.AbstractListWidget;
import org.skyve.impl.metadata.view.widget.bound.tabular.DataGrid;
import org.skyve.impl.metadata.view.widget.bound.tabular.DataGridBoundColumn;
import org.skyve.impl.metadata.view.widget.bound.tabular.DataGridContainerColumn;
import org.skyve.impl.metadata.view.widget.bound.tabular.DataRepeater;
import org.skyve.impl.metadata.view.widget.bound.tabular.ListGrid;
import org.skyve.impl.metadata.view.widget.bound.tabular.ListRepeater;
import org.skyve.impl.metadata.view.widget.bound.tabular.TreeGrid;
import org.skyve.metadata.controller.ImplicitActionName;
import org.skyve.metadata.model.Attribute;
import org.skyve.metadata.model.Attribute.AttributeType;
import org.skyve.metadata.model.document.Bizlet.DomainValue;
import org.skyve.metadata.model.document.Document;
import org.skyve.metadata.model.document.DomainType;
import org.skyve.metadata.model.document.Reference;
import org.skyve.metadata.model.document.Relation;
import org.skyve.metadata.module.Module;
import org.skyve.metadata.module.query.MetaDataQueryColumn;
import org.skyve.metadata.module.query.MetaDataQueryContentColumn;
import org.skyve.metadata.module.query.MetaDataQueryDefinition;
import org.skyve.metadata.module.query.MetaDataQueryProjectedColumn;
import org.skyve.metadata.user.User;
import org.skyve.metadata.view.Action;
import org.skyve.metadata.view.View;
import org.skyve.metadata.view.model.list.DocumentQueryListModel;
import org.skyve.metadata.view.model.list.ListModel;
import org.skyve.util.Binder.TargetMetaData;
import org.skyve.util.Util;

public abstract class ViewRenderer extends ViewVisitor {
	// The user to render for
	protected User user;
	// The locale to render for
	protected Locale locale;

	// Stack of containers sent in to render methods
	private Stack<Container> currentContainers = new Stack<>();
	public Stack<Container> getCurrentContainers() {
		return currentContainers;
	}
	
	// Attributes pushed and popped during internal processing
	private Stack<String> renderAttributes = new Stack<>();
	
	protected ViewRenderer(User user, Module module, Document document, View view) {
		super((CustomerImpl) user.getCustomer(), (ModuleImpl) module, (DocumentImpl) document, (ViewImpl) view);
		this.user = user;
		locale = user.getLocale();
	}

	private String viewTitle;
	private String viewIcon16x16Url;
	private String viewIcon32x32Url;
	
	@Override
	public final void visitView() {
		renderView(viewTitle, viewIcon16x16Url, viewIcon32x32Url);
		currentContainers.push(view);
	}

	public abstract void renderView(String title, String icon16x16Url, String icon32x32Url);
	
	@Override
	public final void visitedView() {
		renderedView(viewTitle, viewIcon16x16Url, viewIcon32x32Url);
		currentContainers.pop();
	}

	public abstract void renderedView(String title, String icon16x16Url, String icon32x32Url);
	
	private TabPane currentTabPane;
	public TabPane getCurrentTabPane() {
		return currentTabPane;
	}
	
	@Override
	public final void visitTabPane(TabPane tabPane, boolean parentVisible, boolean parentEnabled) {
		renderTabPane(tabPane);
		currentTabPane = tabPane;
	}
	
	public abstract void renderTabPane(TabPane tabPane);
	
	@Override
	public final void visitedTabPane(TabPane tabPane, boolean parentVisible, boolean parentEnabled) {
		renderedTabPane(tabPane);
		currentTabPane = null;
	}

	public abstract void renderedTabPane(TabPane tabPane);
	
	@Override
	public final void visitTab(Tab tab, boolean parentVisible, boolean parentEnabled) {
		String title = Util.i18n(tab.getTitle(), locale);
		String icon16x16Url = iconToUrl(tab.getIcon16x16RelativeFileName());
		renderAttributes.push(icon16x16Url);
		renderAttributes.push(title);
		renderTab(title, icon16x16Url, tab);
		currentContainers.push(tab);
	}

	public abstract void renderTab(String title, String icon16x16Url, Tab tab);
	
	@Override
	public final void visitedTab(Tab tab, boolean parentVisible, boolean parentEnabled) {
		renderedTab(renderAttributes.pop(), renderAttributes.pop(), tab);
		currentContainers.pop();
	}
	
	public abstract void renderedTab(String title, String icon16x16Url, Tab tab);

	@Override
	public final void visitVBox(VBox vbox, boolean parentVisible, boolean parentEnabled) {
		String borderTitle = Util.i18n(vbox.getBorderTitle(), locale);
		renderAttributes.push(borderTitle);
		renderVBox(borderTitle, vbox);
		currentContainers.push(vbox);
	}

	public abstract void renderVBox(String borderTitle, VBox vbox);
	
	@Override
	public final void visitedVBox(VBox vbox, boolean parentVisible, boolean parentEnabled) {
		renderedVBox(renderAttributes.pop(), vbox);
		currentContainers.pop();
	}

	public abstract void renderedVBox(String borderTitle, VBox vbox);

	@Override
	public final void visitHBox(HBox hbox, boolean parentVisible, boolean parentEnabled) {
		String borderTitle = Util.i18n(hbox.getBorderTitle(), locale);
		renderAttributes.push(borderTitle);
		renderHBox(borderTitle, hbox);
		currentContainers.push(hbox);
	}

	public abstract void renderHBox(String borderTitle, HBox hbox);

	@Override
	public final void visitedHBox(HBox hbox, boolean parentVisible, boolean parentEnabled) {
		renderedHBox(renderAttributes.pop(), hbox);
		currentContainers.pop();
	}

	public abstract void renderedHBox(String title, HBox bbox);

	private Form currentForm;
	public Form getCurrentForm() {
		return currentForm;
	}
	private String currentFormBorderTitle;
	
	@Override
	public final void visitForm(Form form, boolean parentVisible, boolean parentEnabled) {
		currentForm = form;
		currentFormBorderTitle = Util.i18n(form.getBorderTitle(), locale);
		renderForm(currentFormBorderTitle, form);
	}
	
	public abstract void renderForm(String borderTitle, Form form);

	@Override
	public final void visitedForm(Form form, boolean parentVisible, boolean parentEnabled) {
		renderedForm(currentFormBorderTitle, form);
		
		currentForm = null;
		currentFormBorderTitle = null;
	}

	public abstract void renderedForm(String borderTitle, Form form);

	@Override
	public final void visitFormColumn(FormColumn column, boolean parentVisible, boolean parentEnabled) {
		renderFormColumn(column);
	}
	
	public abstract void renderFormColumn(FormColumn column);

	private FormRow currentFormRow;
	public FormRow getCurrentFormRow() {
		return currentFormRow;
	}
	
	@Override
	public final void visitFormRow(FormRow row, boolean parentVisible, boolean parentEnabled) {
		currentFormRow = row;
		renderFormRow(row);
	}

	public abstract void renderFormRow(FormRow row);

	private FormItem currentFormItem;
	public FormItem getCurrentFormItem() {
		return currentFormItem;
	}
	private String currentWidgetLabel;
	public String getCurrentWidgetLabel() {
		return currentWidgetLabel;
	}
	private Boolean currentWidgetRequired;
	public boolean isCurrentWidgetRequired() {
		return Boolean.TRUE.equals(currentWidgetRequired);
	}
	private String currentWidgetHelp;
	public String getCurrentWidgetHelp() {
		return currentWidgetHelp;
	}
	private TargetMetaData currentTarget;
	public TargetMetaData getCurrentTarget() {
		return currentTarget;
	}
	
	@Override
	public final void visitFormItem(FormItem item, boolean parentVisible, boolean parentEnabled) {
		currentFormItem = item;
	}

	public abstract void renderFormItem(String label,
											boolean required,
											String help,
											FormItem item);
	
	private void preProcessWidget(String binding) {
		currentWidgetLabel = null;
		currentWidgetRequired = null;
		currentWidgetHelp = null;
		currentTarget = null;
		
		if (binding != null) {
			currentTarget = BindUtil.getMetaDataForBinding(customer, module, document, binding);
			Document targetDocument = currentTarget.getDocument(); 
			Attribute targetAttribute = currentTarget.getAttribute();
			if (binding.endsWith(Bean.BIZ_KEY)) {
				if (targetDocument != null) {
					currentWidgetLabel = targetDocument.getSingularAlias();
					currentWidgetHelp = targetDocument.getDescription();
				}
				else {
					Text bizKeyAttribute = DocumentImpl.getBizKeyAttribute();
					currentWidgetLabel = bizKeyAttribute.getDisplayName();
					currentWidgetRequired = bizKeyAttribute.getRequiredBool();
					currentWidgetHelp = bizKeyAttribute.getDescription();
				}
			}
			else if (binding.endsWith(Bean.ORDINAL_NAME)) {
				org.skyve.impl.metadata.model.document.field.Integer bizOrdinalAttribute = DocumentImpl.getBizOrdinalAttribute();
				currentWidgetLabel = bizOrdinalAttribute.getDisplayName();
				currentWidgetRequired = bizOrdinalAttribute.getRequiredBool();
				currentWidgetHelp = bizOrdinalAttribute.getDescription();
			}
			
			if ((targetDocument != null) && (targetAttribute != null)) {
				currentWidgetLabel = targetAttribute.getDisplayName();
				currentWidgetRequired = targetAttribute.isRequired() ? Boolean.TRUE : Boolean.FALSE;
				currentWidgetHelp = targetAttribute.getDescription();
			}
			preProcessWidget();
		}
	}
	
	private void preProcessWidget() {
		if (currentFormItem != null) {
			currentWidgetLabel = currentFormItem.getLabel();
			currentWidgetHelp = currentFormItem.getHelp();
			currentWidgetRequired = currentFormItem.getRequired();
		}
		currentWidgetLabel = Util.i18n(currentWidgetLabel, locale);
		currentWidgetHelp = Util.i18n(currentWidgetHelp, locale);
		if (currentFormItem != null) {
			renderFormItem(currentWidgetLabel,
							Boolean.TRUE.equals(currentWidgetRequired),
							currentWidgetHelp,
							currentFormItem);
		}
	}
	
	@Override
	public final void visitedFormItem(FormItem item, boolean parentVisible, boolean parentEnabled) {
		renderedFormItem(currentWidgetLabel,
							Boolean.TRUE.equals(currentWidgetRequired),
							currentWidgetHelp,
							item);
		currentFormItem = null;
		currentWidgetRequired = null;
		currentWidgetLabel = null;
		currentWidgetHelp = null;
		currentTarget = null;
	}

	public abstract void renderedFormItem(String label,
											boolean required,
											String help,
											FormItem item);
	
	@Override
	public final void visitedFormRow(FormRow row, boolean parentVisible, boolean parentEnabled) {
		renderedFormRow(row);
		currentFormRow = null;
	}

	public abstract void renderedFormRow(FormRow row);

	private String actionLabel;
	private String actionIconUrl;
	private String actionIconStyleClass;
	private String actionToolTip;
	private String actionConfirmationText;
	private char actionType;
	
	/**
	 * @param action
	 * @return	false if the user does not have privileges to execute the action, otherwise true.
	 */
	private boolean preProcessAction(ImplicitActionName implicitName, Action action) {
		String resourceName = action.getResourceName();
		String displayName = action.getDisplayName();
		// Note that the " " result is for SC
		actionLabel = (displayName == null) ? 
						((implicitName == null) ? " " : Util.i18n(implicitName.getDisplayName(), locale)) :
							Util.i18n(displayName, locale);
		String relativeIconFileName = action.getRelativeIconFileName();
		actionIconStyleClass = action.getIconStyleClass();
		actionConfirmationText = action.getConfirmationText();
		String actionConfirmationParam = null;
		
		if (implicitName == null) {
			if (! user.canExecuteAction(document, resourceName)) {
				return false;
			}
			actionType = ' ';
		}
		else {
			switch (implicitName) {
				case Add:
					if (! user.canCreateDocument(document)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Add.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-plus";
					}
					actionType = 'A';
					break;
				case BizExport:
					if (! user.canExecuteAction(document, resourceName)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/BizExport.png";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-cloud-upload";
					}
					actionType = 'X';
					break;
				case BizImport:
					if (! user.canExecuteAction(document, resourceName)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/BizImport.png";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-cloud-download";
					}
					actionType = 'I';
					break;
				case Download:
					if (! user.canExecuteAction(document, resourceName)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Download.png";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-download";
					}
					actionType = 'L';
					break;
				case Upload:
					if (! user.canExecuteAction(document, resourceName)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Upload.png";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-upload";
					}
					actionType = 'U';
					break;
				case Cancel:
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Cancel.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-chevron-left";
					}
					actionType = 'C';
					break;
				case Delete:
					if (! user.canDeleteDocument(document)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Delete.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-trash-o";
					}
					if (actionConfirmationText == null) {
						actionConfirmationText = "ui.delete.confirmation";
						actionConfirmationParam = document.getSingularAlias();
					}
					actionType = 'D';
					break;
				case Edit:
					if (! user.canReadDocument(document)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Edit.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-mail-forward";
					}
					actionType = 'E';
					break;
				case New:
					if (! user.canCreateDocument(document)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/New.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-plus";
					}
					actionType = 'N';
					break;
				case OK:
					if ((! user.canUpdateDocument(document)) && 
							(! user.canCreateDocument(document))) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/OK.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-check";
					}
					actionType = 'O';
					break;
				case Remove:
					if (! user.canDeleteDocument(document)) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Remove.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-minus";
					}
					if (actionConfirmationText == null) {
						actionConfirmationText = "ui.remove.confirmation";
					}
					actionType = 'R';
					break;
				case Report:
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Report.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-newspaper";
					}
					actionType = 'P';
					break;
				case Save:
					if ((! user.canUpdateDocument(document)) && 
							(! user.canCreateDocument(document))) {
						return false;
					}
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Save.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-save";
					}
					actionType = 'S';
					break;
				case ZoomOut:
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/ZoomOut.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-reply";
					}
					actionType = 'Z';
					break;
				case Print:
					if (relativeIconFileName == null) {
						relativeIconFileName = "actions/Report.gif";
					}
					if (actionIconStyleClass == null) {
						actionIconStyleClass = "fa fa-newspaper";
					}
					actionType = 'V';
					break;
				default:
					throw new IllegalArgumentException(implicitName + " not catered for");
			}
		}

		actionIconUrl = iconToUrl(relativeIconFileName);
		actionToolTip = Util.i18n(action.getToolTip(), locale);
		if (actionConfirmationParam != null) {
			actionConfirmationText = Util.i18n(actionConfirmationText, locale, actionConfirmationParam);
		}
		else {
			actionConfirmationText = Util.i18n(actionConfirmationText, locale);
		}
		
		return true;
	}
		
	@Override
	public final void visitButton(Button button, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget();
		Action action = view.getAction(button.getActionName());
		if (preProcessAction(action.getImplicitName(), action)) {
			if (currentFormItem != null) {
				renderFormButton(action,
									actionLabel,
									actionIconUrl,
									actionIconStyleClass,
									actionToolTip,
									actionConfirmationText,
									actionType,
									button);
			}
			else {
				renderButton(action,
								actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								button);
			}
		}
	}

	public abstract void renderFormButton(Action action,
										String label,
										String iconUrl,
										String iconStyleClass,
										String toolTip,
										String confirmationText,
										char type,
										Button button);
	public abstract void renderButton(Action action,
										String label,
										String iconUrl,
										String iconStyleClass,
										String toolTip,
										String confirmationText,
										char type,
										Button button);

	@Override
	public final void visitGeoLocator(GeoLocator locator, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(locator.getDescriptionBinding());
		if (currentFormItem != null) {
			renderFormGeoLocator(locator);
		}
		else {
			renderGeoLocator(locator);
		}
	}

	public abstract void renderFormGeoLocator(GeoLocator locator);
	public abstract void renderGeoLocator(GeoLocator locator);

	@Override
	public final void visitMap(MapDisplay map, boolean parentVisible, boolean parentEnabled) {
		renderMap(map);
	}
	
	public abstract void renderMap(MapDisplay map);
	
	@Override
	public final void visitGeometry(Geometry geometry, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(geometry.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnGeometry(geometry);
		}
		else {
			renderFormGeometry(geometry);
		}
	}

	public abstract void renderBoundColumnGeometry(Geometry geometry);
	public abstract void renderFormGeometry(Geometry geometry);

	@Override
	public final void visitDialogButton(DialogButton button, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget();
		String label = Util.i18n(button.getDisplayName(), locale);
		if (currentFormItem != null) {
			renderFormDialogButton(label, button);
		}
		else {
			renderDialogButton(label, button);
		}
	}

	public abstract void renderFormDialogButton(String label, DialogButton button);
	public abstract void renderDialogButton(String label, DialogButton button);
	
	@Override
	public final void visitSpacer(Spacer spacer) {
		preProcessWidget();
		if (currentFormItem != null) {
			renderFormSpacer(spacer);
		}
		else {
			renderSpacer(spacer);
		}
	}
	
	public abstract void renderFormSpacer(Spacer spacer);
	public abstract void renderSpacer(Spacer spacer);

	@Override
	public final void visitStaticImage(StaticImage image, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget();
		String fileUrl = staticImageToUrl(image.getRelativeFile());
		if (currentFormItem != null) {
			renderFormStaticImage(fileUrl, image);
		}
		else if (currentContainerColumn != null) {
			renderContainerColumnStaticImage(fileUrl, image);
		}
		else {
			renderStaticImage(fileUrl, image);
		}
	}

	public abstract void renderFormStaticImage(String fileUrl, StaticImage image);
	public abstract void renderStaticImage(String fileUrl, StaticImage image);
	public abstract void renderContainerColumnStaticImage(String fileUrl, StaticImage image);
	
	@Override
	public final void visitDynamicImage(DynamicImage image, boolean parentVisible, boolean parentEnabled) {
		if (currentContainerColumn != null) {
			renderContainerColumnDynamicImage(image);
		}
		else {
			renderDynamicImage(image);
		}
	}
	
	public abstract void renderContainerColumnDynamicImage(DynamicImage image);
	public abstract void renderDynamicImage(DynamicImage image);
	
	@Override
	public final void visitLink(Link link, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget();
		String value = Util.i18n(link.getValue(), locale);
		if (currentFormItem != null) {
			renderFormLink(value, link);
		}
		else if (currentContainerColumn != null) {
			renderContainerColumnLink(value, link);
		}
		else {
			renderLink(value, link);
		}
	}
	
	public abstract void renderFormLink(String value, Link link);
	public abstract void renderContainerColumnLink(String value, Link link);
	public abstract void renderLink(String value, Link link);

	@Override
	public final void visitBlurb(Blurb blurb, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget();
		String markup = Util.i18n(blurb.getMarkup(), locale);
		if (currentFormItem != null) {
			renderFormBlurb(markup, blurb);
		}
		else if (currentContainerColumn != null) {
			renderContainerColumnBlurb(markup, blurb);
		}
		else {
			renderBlurb(markup, blurb);
		}
	}

	public abstract void renderFormBlurb(String markup, Blurb blurb);
	public abstract void renderContainerColumnBlurb(String markup, Blurb blurb);
	public abstract void renderBlurb(String markup, Blurb blurb);

	@Override
	public final void visitLabel(Label label, boolean parentVisible, boolean parentEnabled) {
		String value = label.getValue();
		String binding = label.getBinding();
		String faw = label.getFor();
		if (faw != null) {
			preProcessWidget(faw);
			value = currentWidgetLabel;
		}
		else if (binding != null) {
			preProcessWidget(binding);
			value = null;
		}
		else {
			preProcessWidget();
			value = Util.i18n(value, locale);
			currentTarget = null;
		}
		if (currentFormItem != null) {
			renderFormLabel(value, label);
		}
		else if (currentContainerColumn != null) {
			renderContainerColumnLabel(value, label);
		}
		else {
			renderLabel(value, label);
		}
	}

	public abstract void renderFormLabel(String value, Label label);
	public abstract void renderContainerColumnLabel(String value, Label label);
	public abstract void renderLabel(String value, Label label);

	@Override
	public final void visitProgressBar(ProgressBar progressBar, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(progressBar.getBinding());
		renderFormProgressBar(progressBar);
	}
	
	public abstract void renderFormProgressBar(ProgressBar progressBar);
	
	private String tabularTitle;

	private String currentListWidgetModelName;
	public String getCurrentListWidgetModelName() {
		return currentListWidgetModelName;
	}
	private String currentListWidgetModelDocumentName;
	public String getCurrentListWidgetModelDocumentName() {
		return currentListWidgetModelDocumentName;
	}
	private ListModel<? extends Bean> currentListWidgetModel;
	public ListModel<? extends Bean> getCurrentListWidgetModel() {
		return currentListWidgetModel;
	}
	private Document currentListWidgetDrivingDocument;
	public Document getCurrentListWidgetDrivingDocument() {
		return currentListWidgetDrivingDocument;
	}
	
	private void preProcessListWidget(AbstractListWidget widget) {
		tabularTitle = Util.i18n(widget.getTitle(), locale);

		String queryName = widget.getQueryName();
		String modelName = widget.getModelName();
		
		if ((queryName == null) && (modelName != null)) {
			currentListWidgetModelName = modelName;
			currentListWidgetModelDocumentName = document.getName();
			currentListWidgetModel = CORE.getRepository().getListModel(customer, document, currentListWidgetModelName, true);
			currentListWidgetDrivingDocument = currentListWidgetModel.getDrivingDocument();
		}
		else {
			MetaDataQueryDefinition query = module.getMetaDataQuery(queryName);
			if (query == null) {
				query = module.getDocumentDefaultQuery(customer, queryName);
			}
			currentListWidgetModelName = queryName;
			currentListWidgetModelDocumentName = query.getDocumentName();
			currentListWidgetDrivingDocument = query.getDocumentModule(customer).getDocument(customer, currentListWidgetModelDocumentName);
	        DocumentQueryListModel<Bean> queryModel = new DocumentQueryListModel<>();
	        queryModel.setQuery(query);
	        currentListWidgetModel = queryModel;
		}
	}
	
	private void postProcessListWidget() {
		tabularTitle = null;
		currentListWidgetModelName = null;
		currentListWidgetModelDocumentName = null;
		currentListWidgetModel = null;
		currentListWidgetDrivingDocument = null;
	}

	@Override
	public final void visitListGrid(ListGrid grid, boolean parentVisible, boolean parentEnabled) {
		preProcessListWidget(grid);
		renderListGrid(tabularTitle, grid);
		
		for (MetaDataQueryColumn column : currentListWidgetModel.getColumns()) {
			if (column instanceof MetaDataQueryProjectedColumn) {
				renderListGridProjectedColumn((MetaDataQueryProjectedColumn) column);
			}
			else {
				renderListGridContentColumn((MetaDataQueryContentColumn) column);
			}
		}
	}

	public abstract void renderListGrid(String title, ListGrid grid);
	public abstract void renderListGridProjectedColumn(MetaDataQueryProjectedColumn column);
	public abstract void renderListGridContentColumn(MetaDataQueryContentColumn column);

	@Override
	public final void visitedListGrid(ListGrid grid, boolean parentVisible, boolean parentEnabled) {
		renderedListGrid(tabularTitle, grid);
		postProcessListWidget();
	}

	public abstract void renderedListGrid(String title, ListGrid grid);

	@Override
	public final void visitListRepeater(ListRepeater repeater, boolean parentVisible, boolean parentEnabled) {
		preProcessListWidget(repeater);
		renderListRepeater(tabularTitle, repeater);

		for (MetaDataQueryColumn column : currentListWidgetModel.getColumns()) {
			if (column instanceof MetaDataQueryProjectedColumn) {
				renderListRepeaterProjectedColumn((MetaDataQueryProjectedColumn) column);
			}
			else {
				renderListRepeaterContentColumn((MetaDataQueryContentColumn) column);
			}
		}
	}

	public abstract void renderListRepeater(String title, ListRepeater repeater);
	public abstract void renderListRepeaterProjectedColumn(MetaDataQueryProjectedColumn column);
	public abstract void renderListRepeaterContentColumn(MetaDataQueryContentColumn column);

	@Override
	public final void visitedListRepeater(ListRepeater repeater, boolean parentVisible, boolean parentEnabled) {
		renderedListRepeater(tabularTitle, repeater);
		postProcessListWidget();
	}

	public abstract void renderedListRepeater(String title, ListRepeater repeater);

	@Override
	public final void visitTreeGrid(TreeGrid grid, boolean parentVisible, boolean parentEnabled) {
		preProcessListWidget(grid);
		renderTreeGrid(tabularTitle, grid);

		for (MetaDataQueryColumn column : currentListWidgetModel.getColumns()) {
			if (column instanceof MetaDataQueryProjectedColumn) {
				renderTreeGridProjectedColumn((MetaDataQueryProjectedColumn) column);
			}
			else {
				renderTreeGridContentColumn((MetaDataQueryContentColumn) column);
			}
		}
	}

	public abstract void renderTreeGrid(String title, TreeGrid grid);
	public abstract void renderTreeGridProjectedColumn(MetaDataQueryProjectedColumn column);
	public abstract void renderTreeGridContentColumn(MetaDataQueryContentColumn column);

	@Override
	public final void visitedTreeGrid(TreeGrid grid, boolean parentVisible, boolean parentEnabled) {
		renderedTreeGrid(tabularTitle, grid);
		postProcessListWidget();
	}

	public abstract void renderedTreeGrid(String title, TreeGrid grid);

	private AbstractDataWidget currentDataWidget;
	public AbstractDataWidget getCurrentDataWidget() {
		return currentDataWidget;
	}
	
	private TargetMetaData dataWidgetTarget;

	@Override
	public final void visitDataGrid(DataGrid grid, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(grid.getBinding());
		dataWidgetTarget = currentTarget;
		tabularTitle = Util.i18n(grid.getTitle(), locale);
		currentDataWidget = grid;
		renderDataGrid(tabularTitle, grid);
	}

	public abstract void renderDataGrid(String title, DataGrid grid);

	@Override
	public final void visitedDataGrid(DataGrid grid, boolean parentVisible, boolean parentEnabled) {
		currentTarget = dataWidgetTarget;
		renderedDataGrid(tabularTitle, grid);
		dataWidgetTarget = null;
		tabularTitle = null;
		currentDataWidget = null;
	}

	public abstract void renderedDataGrid(String title, DataGrid grid);

	@Override
	public final void visitDataRepeater(DataRepeater repeater, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(repeater.getBinding());
		dataWidgetTarget = currentTarget;
		tabularTitle = Util.i18n(repeater.getTitle(), locale);
		currentDataWidget = repeater;
		renderDataRepeater(tabularTitle, repeater);
	}

	public abstract void renderDataRepeater(String title, DataRepeater repeater);

	@Override
	public final void visitedDataRepeater(DataRepeater repeater, boolean parentVisible, boolean parentEnabled) {
		currentTarget = dataWidgetTarget;
		renderedDataRepeater(tabularTitle, repeater);
		dataWidgetTarget = null;
		tabularTitle = null;
		currentDataWidget = null;
	}

	public abstract void renderedDataRepeater(String title, DataRepeater repeater);

	private String currentColumnTitle;
	public String getCurrentColumnTitle() {
		return currentColumnTitle;
	}
	private DataGridBoundColumn currentBoundColumn;
	public DataGridBoundColumn getCurrentBoundColumn() {
		return currentBoundColumn;
	}
	
	@Override
	public final void visitDataGridBoundColumn(DataGridBoundColumn column, boolean parentVisible, boolean parentEnabled) {
		currentColumnTitle = column.getTitle();
		String binding = column.getBinding();
		if (binding == null) { // binding to the collection
			preProcessWidget(currentDataWidget.getBinding());
		}
		else {
			preProcessWidget(binding);
		}
		if (currentColumnTitle == null) {
			currentColumnTitle = currentWidgetLabel;
		}
		currentColumnTitle = Util.i18n(currentColumnTitle, locale);
		currentBoundColumn = column;
		if (currentDataWidget instanceof DataGrid) {
			renderDataGridBoundColumn(currentColumnTitle, column);
		}
		else {
			renderDataRepeaterBoundColumn(currentColumnTitle, column);
		}
	}

	public abstract void renderDataRepeaterBoundColumn(String title, DataGridBoundColumn column);
	public abstract void renderDataGridBoundColumn(String title, DataGridBoundColumn column);

	@Override
	public final void visitedDataGridBoundColumn(DataGridBoundColumn column, boolean parentVisible, boolean parentEnabled) {
		if (currentDataWidget instanceof DataGrid) {
			renderedDataGridBoundColumn(currentColumnTitle, column);
		}
		else {
			renderedDataRepeaterBoundColumn(currentColumnTitle, column);
		}
		currentColumnTitle = null;
		currentBoundColumn = null;
	}

	public abstract void renderedDataRepeaterBoundColumn(String title, DataGridBoundColumn column);
	public abstract void renderedDataGridBoundColumn(String title, DataGridBoundColumn column);

	private DataGridContainerColumn currentContainerColumn;
	public DataGridContainerColumn getCurrentContainerColumn() {
		return currentContainerColumn;
	}
	
	@Override
	public final void visitDataGridContainerColumn(DataGridContainerColumn column, boolean parentVisible, boolean parentEnabled) {
		currentColumnTitle = Util.i18n(column.getTitle(), locale);
		currentContainerColumn = column;
		if (currentDataWidget instanceof DataGrid) {
			renderDataGridContainerColumn(currentColumnTitle, column);
		}
		else {
			renderDataRepeaterContainerColumn(currentColumnTitle, column);
		}
	}

	public abstract void renderDataRepeaterContainerColumn(String title, DataGridContainerColumn column);
	public abstract void renderDataGridContainerColumn(String title, DataGridContainerColumn column);

	@Override
	public final void visitedDataGridContainerColumn(DataGridContainerColumn column, boolean parentVisible, boolean parentEnabled) {
		if (currentDataWidget instanceof DataGrid) {
			renderedDataRepeaterContainerColumn(currentColumnTitle, column);
		}
		else {
			renderedDataGridContainerColumn(currentColumnTitle, column);
		}
		currentColumnTitle = null;
		currentContainerColumn = null;
	}

	public abstract void renderedDataRepeaterContainerColumn(String title, DataGridContainerColumn column);
	public abstract void renderedDataGridContainerColumn(String title, DataGridContainerColumn column);

	@Override
	public final void visitCheckBox(CheckBox checkBox, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(checkBox.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnCheckBox(checkBox);
		}
		else {
			renderFormCheckBox(checkBox);
		}
	}

	public abstract void renderBoundColumnCheckBox(CheckBox checkBox);
	public abstract void renderFormCheckBox(CheckBox checkBox);

	@Override
	public final void visitedCheckBox(CheckBox checkBox, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnCheckBox(checkBox);
		}
		else {
			renderedFormCheckBox(checkBox);
		}
	}

	public abstract void renderedBoundColumnCheckBox(CheckBox checkBox);
	public abstract void renderedFormCheckBox(CheckBox checkBox);

	@Override
	public final void visitCheckMembership(CheckMembership membership, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(membership.getBinding());
		renderCheckMembership(membership);
	}

	public abstract void renderCheckMembership(CheckMembership membership);

	@Override
	public final void visitedCheckMembership(CheckMembership membership, boolean parentVisible, boolean parentEnabled) {
		renderedCheckMembership(membership);
	}

	public abstract void renderedCheckMembership(CheckMembership membership);

	@Override
	public final void visitColourPicker(ColourPicker colour, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(colour.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnColourPicker(colour);
		}
		else {
			renderFormColourPicker(colour);
		}
	}

	public abstract void renderBoundColumnColourPicker(ColourPicker colour);
	public abstract void renderFormColourPicker(ColourPicker colour);

	@Override
	public final void visitedColourPicker(ColourPicker colour, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnColourPicker(colour);
		}
		else {
			renderedFormColourPicker(colour);
		}
	}

	public abstract void renderedBoundColumnColourPicker(ColourPicker colour);
	public abstract void renderedFormColourPicker(ColourPicker colour);

	@Override
	public final void visitCombo(Combo combo, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(combo.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnCombo(combo);
		}
		else {
			renderFormCombo(combo);
		}
	}
	
	public abstract void renderBoundColumnCombo(Combo combo);
	public abstract void renderFormCombo(Combo combo);

	@Override
	public final void visitedCombo(Combo combo, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnCombo(combo);
		}
		else {
			renderedFormCombo(combo);
		}
	}

	public abstract void renderedBoundColumnCombo(Combo combo);
	public abstract void renderedFormCombo(Combo combo);
	
	@Override
	public final void visitContentImage(ContentImage image, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(image.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnContentImage(image);
		}
		else if (currentContainerColumn != null) {
			renderContainerColumnContentImage(image);
		}
		else {
			renderFormContentImage(image);
		}
	}

	public abstract void renderBoundColumnContentImage(ContentImage image);
	public abstract void renderContainerColumnContentImage(ContentImage image);
	public abstract void renderFormContentImage(ContentImage image);
	
	@Override
	public final void visitContentLink(ContentLink link, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(link.getBinding());
		String value = Util.i18n(link.getValue(), locale);
		if (currentBoundColumn != null) {
			renderBoundColumnContentLink(value, link);
		}
		else {
			renderFormContentLink(value, link);
		}
	}

	public abstract void renderBoundColumnContentLink(String value, ContentLink link);
	public abstract void renderFormContentLink(String value, ContentLink link);

	@Override
	public final void visitHTML(HTML html, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(html.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnHTML(html);
		}
		else {
			renderFormHTML(html);
		}
	}
	
	public abstract void renderBoundColumnHTML(HTML html);
	public abstract void renderFormHTML(HTML html);

	private String listMembershipCandidatesHeading;
	private String listMembershipMembersHeading;
	
	@Override
	public final void visitListMembership(ListMembership membership, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(membership.getBinding());
		listMembershipCandidatesHeading = Util.i18n(membership.getCandidatesHeading(), locale);
		listMembershipMembersHeading = Util.i18n(membership.getMembersHeading(), locale);
		renderListMembership(listMembershipCandidatesHeading,
								listMembershipMembersHeading,
								membership);
	}

	public abstract void renderListMembership(String candidatesHeading,
												String membersHeading,
												ListMembership membership);
	@Override
	public final void visitedListMembership(ListMembership membership, boolean parentVisible, boolean parentEnabled) {
		renderedListMembership(listMembershipCandidatesHeading,
								listMembershipMembersHeading,
								membership);
		
		listMembershipCandidatesHeading = null;
		listMembershipMembersHeading = null;
	}

	public abstract void renderedListMembership(String candidatesHeading,
													String membersHeading,
													ListMembership membership);

	@Override
	public final void visitComparison(Comparison comparison, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(comparison.getBinding());
		renderComparison(comparison);
	}

	public abstract void renderComparison(Comparison comparison);
	
	private MetaDataQueryDefinition currentLookupQuery;
	private boolean currentLookupCanCreate;
	private boolean currentLookupCanUpdate;
	
	private void preProcessLookupWidget(String binding, String widgetQueryName) {
		preProcessWidget(binding);
		String queryName = widgetQueryName;
		// Use reference query name if none provided in the widget
		Attribute targetAttribute = currentTarget.getAttribute();
		if ((queryName == null) && (targetAttribute instanceof Reference)) {
			queryName = ((Reference) targetAttribute).getQueryName();
		}
		// Use the default query if none is defined, else get the named query.
		if ((queryName == null) && (targetAttribute instanceof Relation)) {
			currentLookupQuery = module.getDocumentDefaultQuery(customer, ((Relation) targetAttribute).getDocumentName());
			queryName = currentLookupQuery.getName();
		}
		else {
			currentLookupQuery = module.getMetaDataQuery(queryName);
		}

		Document queryDocument = module.getDocument(customer, currentLookupQuery.getDocumentName());
		currentLookupCanCreate = user.canCreateDocument(queryDocument);
		currentLookupCanUpdate = user.canUpdateDocument(queryDocument);
	}

	private String currentLookupDescriptionBinding;

	@Override
	public final void visitLookupDescription(LookupDescription lookup, boolean parentVisible, boolean parentEnabled) {
		preProcessLookupWidget(lookup.getBinding(), lookup.getQuery());

		currentLookupDescriptionBinding = lookup.getDescriptionBinding();
		if (currentLookupDescriptionBinding == null) {
			currentLookupDescriptionBinding = Bean.BIZ_KEY;
		}

		if (currentBoundColumn != null) {
			renderBoundColumnLookupDescription(currentLookupQuery,
												currentLookupCanCreate,
												currentLookupCanUpdate,
												currentLookupDescriptionBinding,
												lookup);
		}
		else {
			renderFormLookupDescription(currentLookupQuery,
											currentLookupCanCreate,
											currentLookupCanUpdate,
											currentLookupDescriptionBinding,
											lookup);
		}
	}

	public abstract void renderBoundColumnLookupDescription(MetaDataQueryDefinition query,
																boolean canCreate,
																boolean canUpdate,
																String descriptionBinding,
																LookupDescription lookup);
	public abstract void renderFormLookupDescription(MetaDataQueryDefinition query,
														boolean canCreate,
														boolean canUpdate,
														String descriptionBinding,
														LookupDescription lookup);

	@Override
	public final void visitedLookupDescription(LookupDescription lookup, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnLookupDescription(currentLookupQuery,
													currentLookupCanCreate,
													currentLookupCanUpdate,
													currentLookupDescriptionBinding,
													lookup);
		}
		else {
			renderedFormLookupDescription(currentLookupQuery,
											currentLookupCanCreate,
											currentLookupCanUpdate,
											currentLookupDescriptionBinding,
											lookup);
		}
		
		currentLookupQuery = null;
		currentLookupCanCreate = false;
		currentLookupCanUpdate = false;
		currentLookupDescriptionBinding = null;
	}

	public abstract void renderedBoundColumnLookupDescription(MetaDataQueryDefinition query,
																boolean canCreate,
																boolean canUpdate,
																String descriptionBinding,
																LookupDescription lookup);
	public abstract void renderedFormLookupDescription(MetaDataQueryDefinition query,
														boolean canCreate,
														boolean canUpdate,
														String descriptionBinding,
														LookupDescription lookup);

	@Override
	public final void visitLookup(Lookup lookup, boolean parentVisible, boolean parentEnabled) {
		preProcessLookupWidget(lookup.getBinding(), lookup.getQuery());
		renderFormLookup(currentLookupQuery,
							currentLookupCanCreate,
							currentLookupCanUpdate,
							lookup);
	}

	public abstract void renderFormLookup(MetaDataQueryDefinition query,
											boolean canCreate,
											boolean canUpdate,
											Lookup lookup);

	@Override
	public final void visitedLookup(Lookup lookup, boolean parentVisible, boolean parentEnabled) {
		renderedFormLookup(currentLookupQuery,
							currentLookupCanCreate,
							currentLookupCanUpdate,
							lookup);

		currentLookupQuery = null;
		currentLookupCanCreate = false;
		currentLookupCanUpdate = false;
	}

	public abstract void renderedFormLookup(MetaDataQueryDefinition query,
												boolean canCreate,
												boolean canUpdate,
												Lookup lookup);

	@Override
	public final void visitPassword(Password password, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(password.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnPassword(password);
		}
		else {
			renderFormPassword(password);
		}
	}

	public abstract void renderBoundColumnPassword(Password password);
	public abstract void renderFormPassword(Password password);

	@Override
	public final void visitedPassword(Password password, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnPassword(password);
		}
		else {
			renderedFormPassword(password);
		}
	}

	public abstract void renderedBoundColumnPassword(Password password);
	public abstract void renderedFormPassword(Password password);

	@Override
	public final void visitRadio(Radio radio, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(radio.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnRadio(radio);
		}
		else {
			renderFormRadio(radio);
		}
	}

	public abstract void renderBoundColumnRadio(Radio radio);
	public abstract void renderFormRadio(Radio radio);

	@Override
	public final void visitedRadio(Radio radio, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnRadio(radio);
		}
		else {
			renderedFormRadio(radio);
		}
	}

	public abstract void renderedBoundColumnRadio(Radio radio);
	public abstract void renderedFormRadio(Radio radio);

	@Override
	public final void visitRichText(RichText text, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(text.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnRichText(text);
		}
		else {
			renderFormRichText(text);
		}
	}
	
	public abstract void renderBoundColumnRichText(RichText text);
	public abstract void renderFormRichText(RichText text);

	@Override
	public final void visitedRichText(RichText text, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnRichText(text);
		}
		else {
			renderedFormRichText(text);
		}
	}

	public abstract void renderedBoundColumnRichText(RichText text);
	public abstract void renderedFormRichText(RichText text);

	@Override
	public final void visitSlider(Slider slider, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(slider.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnSlider(slider);
		}
		else {
			renderFormSlider(slider);
		}
	}

	public abstract void renderBoundColumnSlider(Slider slider);
	public abstract void renderFormSlider(Slider slider);

	@Override
	public final void visitedSlider(Slider slider, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnSlider(slider);
		}
		else {
			renderedFormSlider(slider);
		}
	}

	public abstract void renderedBoundColumnSlider(Slider slider);
	public abstract void renderedFormSlider(Slider slider);

	@Override
	public final void visitSpinner(Spinner spinner, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(spinner.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnSpinner(spinner);
		}
		else {
			renderFormSpinner(spinner);
		}
	}

	public abstract void renderBoundColumnSpinner(Spinner spinner);
	public abstract void renderFormSpinner(Spinner spinner);

	@Override
	public final void visitedSpinner(Spinner spinner, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnSpinner(spinner);
		}
		else {
			renderedFormSpinner(spinner);
		}
	}

	public abstract void renderedBoundColumnSpinner(Spinner spinner);
	public abstract void renderedFormSpinner(Spinner spinner);

	@Override
	public final void visitTextArea(TextArea text, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(text.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnTextArea(text);
		}
		else {
			renderFormTextArea(text);
		}
	}

	public abstract void renderBoundColumnTextArea(TextArea text);
	public abstract void renderFormTextArea(TextArea text);

	@Override
	public final void visitedTextArea(TextArea text, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnTextArea(text);
		}
		else {
			renderedFormTextArea(text);
		}
	}

	public abstract void renderedBoundColumnTextArea(TextArea text);
	public abstract void renderedFormTextArea(TextArea text);
	
	@Override
	public final void visitTextField(TextField text, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget(text.getBinding());
		if (currentBoundColumn != null) {
			renderBoundColumnTextField(text);
		}
		else {
			renderFormTextField(text);
		}
	}

	public abstract void renderBoundColumnTextField(TextField text);
	public abstract void renderFormTextField(TextField text);

	@Override
	public final void visitedTextField(TextField text, boolean parentVisible, boolean parentEnabled) {
		if (currentBoundColumn != null) {
			renderedBoundColumnTextField(text);
		}
		else {
			renderedFormTextField(text);
		}
	}

	public abstract void renderedBoundColumnTextField(TextField text);
	public abstract void renderedFormTextField(TextField text);
	
	@Override
	public final void visitInject(Inject inject, boolean parentVisible, boolean parentEnabled) {
		preProcessWidget();
		if (currentFormItem != null) {
			renderFormInject(inject);
		}
		else {
			renderInject(inject);
		}
	}
	
	public abstract void renderFormInject(Inject inject);
	public abstract void renderInject(Inject inject);


	@Override
	public final void visitServerSideActionEventAction(ServerSideActionEventAction server, boolean parentVisible, boolean parentEnabled) {
		Action action = view.getAction(server.getActionName());
		visitServerSideActionEventAction(action, server);
	}
	
	public abstract void visitServerSideActionEventAction(Action action, ServerSideActionEventAction server);

	@Override
	public final void visitCustomAction(ActionImpl action) {
		if (preProcessAction(null, action)) {
			renderCustomAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderCustomAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitAddAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Add, action)) {
			renderAddAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}
	
	public abstract void renderAddAction(String label,
											String iconUrl,
											String iconStyleClass,
											String toolTip,
											String confirmationText,
											char type,
											ActionImpl action);

	@Override
	public final void visitRemoveAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Remove, action)) {
			renderRemoveAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderRemoveAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitZoomOutAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.ZoomOut, action)) {
			renderZoomOutAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderZoomOutAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);
	
	@Override
	public final void visitNavigateAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Navigate, action)) {
			renderNavigateAction(actionLabel,
									actionIconUrl,
									actionIconStyleClass,
									actionToolTip,
									actionConfirmationText,
									actionType,
									action);
		}
	}

	public abstract void renderNavigateAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitOKAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.OK, action)) {
			renderOKAction(actionLabel,
							actionIconUrl,
							actionIconStyleClass,
							actionToolTip,
							actionConfirmationText,
							actionType,
							action);
		}
	}

	public abstract void renderOKAction(String label,
											String iconUrl,
											String iconStyleClass,
											String toolTip,
											String confirmationText,
											char type,
											ActionImpl action);

	@Override
	public final void visitSaveAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Save, action)) {
			renderSaveAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderSaveAction(String label,
											String iconUrl,
											String iconStyleClass,
											String toolTip,
											String confirmationText,
											char type,
											ActionImpl action);

	@Override
	public final void visitCancelAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Cancel, action)) {
			renderCancelAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderCancelAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitDeleteAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Delete, action)) {
			renderDeleteAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderDeleteAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitReportAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Report, action)) {
			renderReportAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderReportAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitBizExportAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.BizExport, action)) {
			renderBizExportAction(actionLabel,
									actionIconUrl,
									actionIconStyleClass,
									actionToolTip,
									actionConfirmationText,
									actionType,
									action);
		}
	}

	public abstract void renderBizExportAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitBizImportAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.BizImport, action)) {
			renderBizImportAction(actionLabel,
									actionIconUrl,
									actionIconStyleClass,
									actionToolTip,
									actionConfirmationText,
									actionType,
									action);
		}
	}

	public abstract void renderBizImportAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitDownloadAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Download, action)) {
			renderDownloadAction(actionLabel,
									actionIconUrl,
									actionIconStyleClass,
									actionToolTip,
									actionConfirmationText,
									actionType,
									action);
		}
	}

	public abstract void renderDownloadAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitUploadAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Upload, action)) {
			renderUploadAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderUploadAction(String label,
												String iconUrl,
												String iconStyleClass,
												String toolTip,
												String confirmationText,
												char type,
												ActionImpl action);

	@Override
	public final void visitNewAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.New, action)) {
			renderNewAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderNewAction(String label,
											String iconUrl,
											String iconStyleClass,
											String toolTip,
											String confirmationText,
											char type,
											ActionImpl action);

	@Override
	public final void visitEditAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Edit, action)) {
			renderEditAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}

	public abstract void renderEditAction(String label,
											String iconUrl,
											String iconStyleClass,
											String toolTip,
											String confirmationText,
											char type,
											ActionImpl action);

	@Override
	public final void visitPrintAction(ActionImpl action) {
		if (preProcessAction(ImplicitActionName.Print, action)) {
			renderPrintAction(actionLabel,
								actionIconUrl,
								actionIconStyleClass,
								actionToolTip,
								actionConfirmationText,
								actionType,
								action);
		}
	}
	
	public abstract void renderPrintAction(String label,
											String iconUrl,
											String iconStyleClass,
											String toolTip,
											String confirmationText,
											char type,
											ActionImpl action);

	protected LinkedHashMap<String, String> getLocalisedConstantDomainValueMap(Attribute attribute) {
		List<DomainValue> values = document.getDomainValues(customer, 
																DomainType.constant, 
																attribute, 
																null,
																true);
		LinkedHashMap<String, String> result = new LinkedHashMap<>(values.size());
		for (DomainValue value : values) {
			result.put(value.getCode(), Util.i18n(value.getDescription(), locale));
		}
		
		return result;
	}

	public static HorizontalAlignment determineDefaultColumnAlignment(AttributeType attributeType) {
		if (AttributeType.date.equals(attributeType) || 
				AttributeType.dateTime.equals(attributeType) ||
				AttributeType.time.equals(attributeType) || 
				AttributeType.timestamp.equals(attributeType) ||
				AttributeType.decimal2.equals(attributeType) || 
				AttributeType.decimal5.equals(attributeType) ||
				AttributeType.decimal10.equals(attributeType) || 
				AttributeType.integer.equals(attributeType) ||
				AttributeType.longInteger.equals(attributeType)) {
			return HorizontalAlignment.right;
		}
		if (AttributeType.bool.equals(attributeType) || 
				AttributeType.content.equals(attributeType)) {
			return HorizontalAlignment.centre;
		}
		return HorizontalAlignment.left;
	}
	
	public abstract Integer determineDefaultColumnWidth(AttributeType attributeType);
	
	private String iconToUrl(String icon) {
		if (icon == null) {
			return null;
		}
		return String.format("resources?_doc=%s.%s&_n=%s", module.getName(), document.getName(), icon);
	}
	
	private static String staticImageToUrl(String imagePath) {
		return "images/" + imagePath;
	}
}