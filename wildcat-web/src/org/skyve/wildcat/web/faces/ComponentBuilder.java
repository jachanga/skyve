package org.skyve.wildcat.web.faces;

import java.util.List;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.primefaces.behavior.ajax.AjaxBehavior;
import org.primefaces.behavior.ajax.AjaxBehaviorListenerImpl;
import org.primefaces.component.accordionpanel.AccordionPanel;
import org.primefaces.component.autocomplete.AutoComplete;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.celleditor.CellEditor;
import org.primefaces.component.colorpicker.ColorPicker;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.commandlink.CommandLink;
import org.primefaces.component.datalist.DataList;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.editor.Editor;
import org.primefaces.component.fieldset.Fieldset;
import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.component.graphicimage.GraphicImage;
import org.primefaces.component.inputmask.InputMask;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.message.Message;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.outputpanel.OutputPanel;
import org.primefaces.component.panel.Panel;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.component.password.Password;
import org.primefaces.component.progressbar.ProgressBar;
import org.primefaces.component.row.Row;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.component.selectmanycheckbox.SelectManyCheckbox;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.selectoneradio.SelectOneRadio;
import org.primefaces.component.spacer.Spacer;
import org.primefaces.component.spinner.Spinner;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.component.toolbar.Toolbar;
import org.primefaces.extensions.component.tristatecheckbox.TriStateCheckbox;
import org.primefaces.mobile.component.field.Field;
import org.skyve.domain.Bean;
import org.skyve.domain.types.converters.Format.TextCase;
import org.skyve.metadata.controller.ImplicitActionName;
import org.skyve.metadata.module.query.Query;
import org.skyve.wildcat.bind.BindUtil;
import org.skyve.wildcat.metadata.model.document.field.TextFormat;
import org.skyve.wildcat.metadata.view.HorizontalAlignment;
import org.skyve.wildcat.metadata.view.reference.ReferenceTarget;
import org.skyve.wildcat.metadata.view.reference.ReferenceTarget.ReferenceTargetType;
import org.skyve.wildcat.web.faces.beans.FacesView;
import org.skyve.wildcat.web.faces.converters.select.AssociationAutoCompleteConverter;
import org.skyve.wildcat.web.faces.converters.select.SelectItemsBeanConverter;

public class ComponentBuilder {
    private FacesContext fc = FacesContext.getCurrentInstance();
    private Application a = fc.getApplication();
    private ExpressionFactory ef = a.getExpressionFactory();
    private ELContext elc = fc.getELContext();
    private String managedBeanName;
	private FacesView<?> managedBean;
	private String process = "@form";
	private String update = "@(form)";
	
	public ComponentBuilder(String managedBeanName, String process, String update) {
		this.managedBeanName = managedBeanName;
		managedBean = FacesUtil.getManagedBean(managedBeanName);
		if (process != null) {
			this.process = process;
		}
		if (update != null) {
			this.update = update;
		}
	}

	private void setId(UIComponent component) {
		component.setId(managedBean.nextId());
//System.out.println(component.getId() + " : " + component);
	}

	public PanelGrid panelGrid(Integer pixelWidth,
                                Integer percentageWidth,
                                Integer pixelHeight,
                                Integer percentageHeight,
                                String invisible) {
		PanelGrid result = (PanelGrid) a.createComponent(PanelGrid.COMPONENT_TYPE);
        addInvisible(result, invisible, null);
        addSize(result, null, pixelWidth, percentageWidth, pixelHeight, percentageHeight, Boolean.FALSE);
        setId(result);
		return result;
	}

	public Row row() {
		Row result = (Row) a.createComponent(Row.COMPONENT_TYPE);
		setId(result);
		return result;
	}
	
	public Column column(String invisible,
							boolean noWrap,
							boolean top,
							Integer pixelWidth,
							Integer percentageWidth,
							Integer colspan,
							Integer rowspan) {
	    Column result = (Column) a.createComponent(Column.COMPONENT_TYPE);
	    addInvisible(result, invisible, null);
	    setId(result);
		if (colspan != null) {
			result.setColspan(colspan.intValue());
		}
		if (rowspan != null) {
			result.setRowspan(rowspan.intValue());
		}
		
		String existingStyle = noWrap ? 
								(top ? "white-space:nowrap;vertical-align:top !important;" : "white-space:nowrap;") :
								(top ? "vertical-align:top !important;" : null);
		addSize(result, existingStyle, pixelWidth, percentageWidth, null, null, null);
		
	    return result;
	}

	public HtmlForm form() {
		HtmlForm result = (HtmlForm) a.createComponent(HtmlForm.COMPONENT_TYPE);
		setId(result);

	    return result;
	}

	public HtmlPanelGroup panelGroup(boolean nowrap, boolean middle, String invisible) {
		HtmlPanelGroup result = (HtmlPanelGroup) a.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
		StringBuilder style = new StringBuilder(32);
		if (nowrap) {
			style.append("white-space:nowrap;");
		}
		if (middle) {
			style.append("vertical-align:middle;");
		}
		if (style.length() > 0) {
			result.setStyle(style.toString());
		}
        addInvisible(result, invisible, null);
        setId(result);
		return result;
	}

	public Fieldset fieldset(String legend, String invisible) {
		Fieldset result = (Fieldset) a.createComponent(Fieldset.COMPONENT_TYPE);
		if (legend != null) {
			result.setLegend(legend);
		}
		addInvisible(result, invisible, null);
		setId(result);
		return result;
	}

	public Panel panel(String title, String invisible, Integer pixelWidth) {
		Panel result = (Panel) a.createComponent(Panel.COMPONENT_TYPE);
		if (title != null) {
			result.setHeader(title);
		}

		addInvisible(result, invisible, null);
		addSize(result, "border:solid 1px gray !important;", pixelWidth, null, null, null, Boolean.FALSE);
		setId(result);
		return result;
	}

	public HtmlOutputText text(String value, 
								String binding,
								HorizontalAlignment textAlignment,
								Integer pixelWidth,
								Integer pixelHeight,
								boolean escape) {
		HtmlOutputText result = (HtmlOutputText) a.createComponent(HtmlOutputText.COMPONENT_TYPE); 
		setId(result);
		if (value != null) {
			result.setValue(value);
		}
		else {
			result.setValueExpression("value", createValueExpressionFromBinding(binding, true, null, Object.class));
		}
		result.setEscape(escape);
		
		String style = null;
		if (HorizontalAlignment.left.equals(textAlignment)) {
			style = "text-align:left;";
		}
		else if (HorizontalAlignment.centre.equals(textAlignment)) {
			style = "text-align:center;";
		}
		else if (HorizontalAlignment.right.equals(textAlignment)) {
			style = "text-align:right;";
		}
		
		addSize(result, style, pixelWidth, null, pixelHeight, null, null);
		
		return result;
	}
	
	public HtmlOutputLabel label(String listBinding, String binding, String value, String forId) {
		HtmlOutputLabel result = null;
		if (forId != null) {
		    result = (OutputLabel) a.createComponent(OutputLabel.COMPONENT_TYPE);
			result.setValue(value + " :");
			setId(result);
			result.setFor(forId);
//    		result.setValueExpression("for", ef.createValueExpression(elc, 
//        																forId,
//        																String.class));
		}
		else {
			result = (HtmlOutputLabel) a.createComponent(HtmlOutputLabel.COMPONENT_TYPE); 
			setId(result);
			if (value != null) {
				result.setValue(value);
			}
			else {
				result.setValueExpression("value", createValueExpressionFromBinding(binding, true, null, Object.class));
			}
		}
		return result;
	}

	public Password password(String bindingPrefix,
								String binding,
								String title,
								boolean required,
								String disabled,
								Integer pixelWidth) {
		Password result = (Password) input(Password.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
		addSize(result, null, pixelWidth, null, null, null, Boolean.TRUE);
		return result;
	}
	
	public InputText textField(String bindingPrefix,
								String binding,
								String title,
								boolean required,
								String disabled,
								Converter converter,
								Integer pixelWidth) {
		InputText result = (InputText) input(InputText.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
		if (converter != null) {
			result.setConverter(converter);
		}
		addSize(result, null, pixelWidth, null, null, null, Boolean.TRUE);
		return result;
	}

	public InputMask maskField(String bindingPrefix,
								String binding,
								String title,
								boolean required,
								String disabled,
								TextFormat format,
								Converter converter,
								Integer pixelWidth) {
		InputMask result = (InputMask) input(InputMask.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
		result.setMask(determineMask(format));
		String existingStyle = null;
		TextCase textCase = format.getCase();
		if (textCase != null) {
			switch (textCase) {
			case upper:
				existingStyle = "text-transform:uppercase;";
				break;
			case capital:
				existingStyle = "text-transform:capitalize;";
				break;
			case lower:
				existingStyle = "text-transform:lowercase;";
				break;
			default:
				throw new IllegalStateException(textCase + " is not supported");
			}
		}
		if (converter != null) {
			result.setConverter(converter);
		}
		addSize(result, existingStyle, pixelWidth, null, null, null, Boolean.TRUE);
		return result;
	}

	/**
	 * My spec is
	 * A - alphanumeric
	 * # - digit
	 * L - letter
	 * 
	 * PF spec is 
	 * Character	Description
	 * 9	Digit (0 through 9)
	 * a    Letter (A through Z)
	 * *	Letter (A through Z) or number (0 through 9)
	 * ?    Allow optional matching of the rest of the expression
	 * 
	 * This method escapes anything that should be literal and then 
	 * converts the expression taking into consideration the case setting.
	 * 
	 * @param text
	 * @return
	 */
	private static String determineMask(TextFormat format) {
		String result = null;
		
		if (format != null) {
			result = format.getMask();
			if (result != null) {
				// first escape characters with meaning
				result = result.replace("9", "\\9");
				result = result.replace("a", "\\a");
				result = result.replace("*", "\\*");
				result = result.replace("?", "\\?");
	
				// transpose my spec to the PF spec
				result = result.replace("A", "*");
				result = result.replace("#", "9");
				result = result.replace("L", "a");
			}
		}
		
		return result;
	}

	public Spinner spinner(String bindingPrefix,
								String binding,
								String title,
								boolean required,
								String disabled,
								Integer pixelWidth) {
		Spinner result = (Spinner) input(Spinner.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
		addSize(result, null, pixelWidth, null, null, null, null);
		return result;
	}

	public Calendar calendar(String bindingPrefix,
								String binding,
	                            String title,
	                            boolean required,
	                            boolean mobile,
	                            String disabled,
	                            Converter converter) {
        Calendar result = (Calendar) input(Calendar.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
        if (! mobile) {
            result.setMode("popup");
	        result.setShowOn("button");
	        result.setNavigator(true);
	        result.setShowButtonPanel(true);
        }
        
        String converterName = converter.getClass().getSimpleName();
        if ("DD_MM_YYYY".equals(converterName)) {
            result.setPattern("dd/MM/yyyy");
            result.setMask("99/99/9999");
        }
        else if ("DD_MMM_YYYY".equals(converterName)) {
            result.setPattern("dd-MMM-yyyy");
            result.setMask("99-aaa-9999");
        }
        else if ("DD_MM_YYYY_HH_MI".equals(converterName)) {
            result.setPattern("dd/MM/yyyy hh:mm");
            result.setMask("99/99/9999 99:99");
        }
        else if ("DD_MM_YYYY_HH24_MI".equals(converterName)) {
            result.setPattern("dd/MM/yyyy HH:mm");
            result.setMask("99/99/9999 99:99");
        }
        else if ("DD_MM_YYYY_HH_MI".equals(converterName)) {
            result.setPattern("dd-MMM-yyyy hh:mm");
            result.setMask("99-aaa-9999 99:99");
        }
        else if ("DD_MM_YYYY_HH24_MI".equals(converterName)) {
            result.setPattern("dd-MMM-yyyy HH:mm");
            result.setMask("99-aaa-9999 99:99");
        }
        else if ("DD_MM_YYYY_HH_MI_SS".equals(converterName)) {
            result.setPattern("dd/MM/yyyy hh:mm:ss");
            result.setMask("99/99/9999 99:99:99");
        }
        else if ("DD_MM_YYYY_HH24_MI_SS".equals(converterName)) {
            result.setPattern("dd/MM/yyyy HH:mm:ss");
            result.setMask("99/99/9999 99:99:99");
        }
        else if ("DD_MM_YYYY_HH_MI_SS".equals(converterName)) {
            result.setPattern("dd-MMM-yyyy hh:mm:ss");
            result.setMask("99-aaa-9999 99:99:99");
        }
        else if ("DD_MM_YYYY_HH24_MI_SS".equals(converterName)) {
            result.setPattern("dd-MMM-yyyy HH:mm:ss");
            result.setMask("99-aaa-9999 99:99:99");
        }
        result.setConverter(converter);
        return result;
	}

	public InputTextarea textArea(String bindingPrefix,
									String binding,
									String title,
									boolean required,
									String disabled,
									Integer pixelWidth,
									Integer pixelHeight) {
	    InputTextarea result = (InputTextarea) input(InputTextarea.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
		addSize(result, null, pixelWidth, null, pixelHeight, null, Boolean.TRUE);
		return result;
	}

    public Message message(String forId) {
        Message message = (Message) a.createComponent(Message.COMPONENT_TYPE);
        setId(message);
        message.setFor(forId);
        message.setShowDetail(true);
        message.setShowSummary(false);
        message.setDisplay("icon");

        return message;
    }

    public Toolbar toolbar() {
    	Toolbar result = (Toolbar) a.createComponent(Toolbar.COMPONENT_TYPE);
    	setId(result);
    	return result;
    }
    
    public Field field(String invisible) {
    	Field result = (Field) a.createComponent(Field.COMPONENT_TYPE);
    	setId(result);
    	addInvisible(result, invisible, null);
    	return result;
    }
    
    public TabView tabView(String invisible) {
        TabView result = (TabView) a.createComponent(TabView.COMPONENT_TYPE);
        addInvisible(result, invisible, null);
        setId(result);
//result.setDynamic(true);
        return result;
    }
    
    public Tab tab(String title,
                            String disabled,
                            String invisible) {
        Tab result = (Tab) a.createComponent(Tab.COMPONENT_TYPE);
        result.setTitle(title);
        addDisabled(result, disabled);
        addInvisible(result, invisible, null);
        setId(result);
        return result;
    }
    
    public CommandButton actionButton(String title,
		    							String tooltip,
		    							ImplicitActionName implicitActionName,
		    							String actionName,
		    							String listBinding,
		                                Integer pixelWidth,
		                                Integer pixelHeight,
		                                Boolean clientValidation,
		                                String disabled,
		                                String invisible) {
        CommandButton result = (CommandButton) a.createComponent(CommandButton.COMPONENT_TYPE);

        result.setValue(title);
        result.setTitle(tooltip);

        action(result, implicitActionName, actionName, listBinding, clientValidation);
        
        addSize(result, null, pixelWidth, null, pixelHeight, null, null);
        addDisabled(result, disabled);
        setId(result);

        // show/hide the implicit buttons - TODO base this also on security privileges.
    	if (ImplicitActionName.OK.equals(implicitActionName) ||
    			ImplicitActionName.Save.equals(implicitActionName) ||
    			ImplicitActionName.Cancel.equals(implicitActionName) ||
    			ImplicitActionName.Delete.equals(implicitActionName)) {
    		StringBuilder expression = new StringBuilder(128);
    		expression.append("empty ").append(managedBeanName).append(".viewBinding");
    		if (invisible == null) {
    			result.setValueExpression("rendered", 
    										createValueExpressionFromBinding(null, 
    																			expression.toString(), 
    																			false, 
    																			null,
    																			Boolean.class));
    		}
    		else {
                addInvisible(result, invisible, expression.toString());
    		}
    	}
    	else if (ImplicitActionName.ZoomOut.equals(implicitActionName) ||
    				ImplicitActionName.Remove.equals(implicitActionName)) {
    		StringBuilder expression = new StringBuilder(128);
    		expression.append("not empty ").append(managedBeanName).append(".viewBinding");
    		if (invisible == null) {
    			result.setValueExpression("rendered", createValueExpressionFromBinding(null,
    																					expression.toString(),
    																					false,
    																					null,
    																					Boolean.class));
    		}
    		else {
    			addInvisible(result, invisible, expression.toString());
    		}
    	}
    	else {
    		addInvisible(result, invisible, null);
    	}
        
        if (ImplicitActionName.Cancel.equals(implicitActionName)) {
			result.setImmediate(true); // no validation
			result.setAjax(false); // normal request - which is slightly faster
		}
		else if (ImplicitActionName.Remove.equals(implicitActionName) ||
					ImplicitActionName.Delete.equals(implicitActionName)) {
			result.setImmediate(true); // no validation
			result.setProcess(process); // process the current form
			result.setUpdate(update); // update all forms
		}
		else {
			result.setProcess(process); // process the current form
			result.setUpdate(update); // update all forms
		}
		
        return result;
    }

    public CommandLink actionLink(String title,
		    							String tooltip,
		    							ImplicitActionName implicitActionName,
		    							String actionName,
		    							String collectionName,
		                                Integer pixelWidth,
		                                Integer pixelHeight,
		                                Boolean clientValidation,
		                                String disabled,
		                                String invisible) {
        CommandLink result = (CommandLink) a.createComponent(CommandLink.COMPONENT_TYPE);

        result.setValue(title);
        result.setTitle(tooltip);

        action(result, implicitActionName, actionName, collectionName, clientValidation);
        
        addSize(result, null, pixelWidth, null, pixelHeight, null, null);
        addDisabled(result, disabled);
        addInvisible(result, invisible, null);
        setId(result);

		if (ImplicitActionName.Cancel.equals(implicitActionName) || ImplicitActionName.OK.equals(implicitActionName)) {
			result.setAjax(false);
		}
		else {
			result.setProcess(process);
			result.setUpdate(update);
		}

        return result;
    }

	public AjaxBehavior ajax(String actionName, String listBinding) {
		AjaxBehavior result = (AjaxBehavior) a.createBehavior(AjaxBehavior.BEHAVIOR_ID);
		result.setProcess(process);
		result.setUpdate(update);
		if (actionName != null) {
			MethodExpression me = methodExpressionForAction(null, actionName, listBinding);
			result.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(me, me));
		}
		
		return result;
	}
    
	public UIParameter parameter(String name, Object value) {
		UIParameter result = (UIParameter) a.createComponent(UIParameter.COMPONENT_TYPE);
		result.setName(name);
		result.setValue(value);
		return result;
	}
	
	private void action(UICommand command,
							ImplicitActionName implicitActionName,
							String actionName,
							String collectionName,
							Boolean clientValidation) {
		command.setActionExpression(methodExpressionForAction(implicitActionName, actionName, collectionName));
        if (Boolean.FALSE.equals(clientValidation)) {
        	command.setValueExpression("immediate", ef.createValueExpression(Boolean.TRUE, Boolean.class));
        }
    }

    private MethodExpression methodExpressionForAction(ImplicitActionName implicitActionName,
														String actionName,
														String collectionName) {
		StringBuilder expression = new StringBuilder(64);
		expression.append("#{").append(managedBeanName).append('.');
		Class<?>[] parameterTypes = null;
		if (implicitActionName != null) {
			expression.append(implicitActionName.toString().toLowerCase());
			if (collectionName != null) {
				if (ImplicitActionName.Add.equals(implicitActionName)) {
					parameterTypes = new Class[] {String.class};
					expression.append("('").append(collectionName).append("')");
				}
				else {
					parameterTypes = new Class[] {String.class, String.class};
					expression.append("('").append(collectionName).append("', ").append(collectionName).append("['").append(Bean.DOCUMENT_ID).append("'])");
				}
			}
			else {
				parameterTypes = new Class[0];
			}
		}
		else {
			parameterTypes = new Class[] {String.class, String.class, String.class};
			expression.append("action('").append(actionName).append('\'');
			if (collectionName != null) {
				expression.append(", '").append(collectionName).append("', ").append(collectionName).append("['").append(Bean.DOCUMENT_ID).append("'])");
			}
			else {
				parameterTypes = new Class[] {String.class};
				expression.append(", null, null)");
			}
		}
		expression.append('}');
//System.out.println("MEL = " + expression.toString());
		return ef.createMethodExpression(elc,
											expression.toString(),
											null,
											parameterTypes);
    }
    
    /**
     *   <h:link outcome="reviewBatch"
     *               value="Restart"
     *               rendered="#{batch.renderRestart}">
     *       <f:param name="c" value="#{batch.row.batchHeader.identifier.clientId}" />
     *       <f:param name="b" value="#{batch.row.batchHeader.identifier.batchNumber}" />
     *   </h:link>
     */
    public HtmlOutputLink outputLink(String value,
                                        String outcome,
                                        String disabled,
                                        String invisible) {
        HtmlOutputLink result = (HtmlOutputLink) a.createComponent(HtmlOutputLink.COMPONENT_TYPE);
        result.setValue(value);
        addDisabled(result, disabled);
        addInvisible(result, invisible, null);
        setId(result);
        return result;
    }

    public HtmlOutputLink outputLink(String listBinding,
    									String value,
    									String href,
    									String invisible,
    									ReferenceTarget target) {
		UIOutput outputText = (UIOutput) a.createComponent(UIOutput.COMPONENT_TYPE);
		outputText.setValue(value);
		HtmlOutputLink result = (HtmlOutputLink) a.createComponent(HtmlOutputLink.COMPONENT_TYPE);
		if (listBinding != null) {
			result.setValueExpression("value", createValueExpressionFromBinding(listBinding, href, true, null, String.class));
		}
		else {
			result.setValueExpression("value", createValueExpressionFromBinding(href, true, null, String.class));
		}
//		ef.createValueExpression(elc, href, String.class));
		result.getChildren().add(outputText);
		addInvisible(result, invisible, null);

		if (target != null) {
			// modal windows are not supported
			ReferenceTargetType type = target.getType();
			if (ReferenceTargetType.blankFrame.equals(type)) {
				result.setTarget("_blank");
			}
			else if (ReferenceTargetType.namedFame.equals(type)) {
				result.setTarget(target.getName());
			}
		}
		
		return result;
    }
    
    public Spacer spacer() {
        Spacer result = (Spacer) a.createComponent(Spacer.COMPONENT_TYPE);
        setId(result);
        return result;
    }
    
    public GraphicImage graphicImage(Integer pixelWidth,
                                        Integer percentageWidth,
                                        Integer pixelHeight,
                                        Integer percentageHeight,
                                        String url,
                                        String invisible) {
        GraphicImage result = (GraphicImage) a.createComponent(GraphicImage.COMPONENT_TYPE);
        result.setUrl(url);
        addSize(result, "border:1px solid gray;", pixelWidth, percentageWidth, pixelHeight, percentageHeight, null);
        addInvisible(result, invisible, null);
        setId(result);
        return result;
    }

	public GraphicImage contentGraphicImage(Integer pixelWidth,
												Integer percentageWidth,
												Integer pixelHeight,
												Integer percentageHeight,
												String binding,
												String invisible) {
		GraphicImage result = (GraphicImage) a.createComponent(GraphicImage.COMPONENT_TYPE);
		
		StringBuilder expression = new StringBuilder(64);
		expression.append("#{").append(managedBeanName).append(".getResourceUrl(").append(managedBeanName).append(".currentBean['");
		expression.append(binding).append("'])}");

		result.setValueExpression("url", ef.createValueExpression(elc, expression.toString(), String.class));
		addSize(result, "border:1px solid gray;", pixelWidth, percentageWidth, pixelHeight, percentageHeight, null);
		addInvisible(result, invisible, null);
		setId(result);
		return result;
	}

    public ProgressBar progressBar() {
        ProgressBar result = (ProgressBar) a.createComponent(ProgressBar.COMPONENT_TYPE);
        setId(result);
        return result;
    }
    
    // TODO do the grids
    
    public SelectBooleanCheckbox checkbox(String bindingPrefix,
    										String binding,
                                            String title,
                                            boolean required,
                                            String disabled,
                                            boolean mobile) {
    	SelectBooleanCheckbox result = (SelectBooleanCheckbox) input(SelectBooleanCheckbox.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
    	if (mobile) {
    		result.setItemLabel(title);
    	}
    	return result;
    }
    
    public TriStateCheckbox triStateCheckbox(String bindingPrefix,
												String binding,
	                                            String title,
	                                            boolean required,
	                                            String disabled) {
        return (TriStateCheckbox) input(TriStateCheckbox.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
    }
    
    public SelectManyCheckbox manyCheckbox(String bindingPrefix,
											String binding,
                                            String title,
                                            boolean required,
                                            String disabled) {
        return (SelectManyCheckbox) input(SelectManyCheckbox.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
    }

    public ColorPicker colourPicker(String bindingPrefix,
										String binding,
                                        String title,
                                        boolean required,
                                        Integer pixelWidth) {
    	ColorPicker result = (ColorPicker) input(ColorPicker.COMPONENT_TYPE, bindingPrefix, binding, title, required, null);
    	addSize(result, null, pixelWidth, null, null, null, Boolean.TRUE);
    	return result;
    }
    
    public HtmlSelectOneMenu selectOneMenu(String bindingPrefix,
	    									String binding,
	                                        String title,
	                                        boolean required,
	                                        String disabled,
    										Integer pixelWidth) {
    	HtmlSelectOneMenu result = (HtmlSelectOneMenu) input(SelectOneMenu.COMPONENT_TYPE,
    															bindingPrefix,
    															binding,
    															title,
    															required,
    															disabled);
    	// Do not default pixel width to 100% as it causes renderering issues on the drop button on the end.
    	// The control sets its width by default based on the font metrics of the drop-down values.
    	addSize(result, null, pixelWidth, null, null, null, null); 
        result.setConverter(new SelectItemsBeanConverter());
        return result;
    }
    
    public SelectOneRadio selectOneRadio(String bindingPrefix,
											String binding,
								            String title,
								            boolean required,
								            String disabled) {
    	SelectOneRadio result = (SelectOneRadio) input(SelectOneRadio.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
        result.setConverter(new SelectItemsBeanConverter());
        return result;
	}
    
    public AutoComplete autoComplete(String bindingPrefix,
										String binding,
    									String title,
    									boolean required,
    									String disabled,
    									String displayBinding,
    									Query query,
    									Integer pixelWidth) {
    	AutoComplete result = (AutoComplete) input(AutoComplete.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
    	result.setForceSelection(true);
    	result.setDropdown(true);
    	result.setVar(binding);
    	StringBuilder expression = new StringBuilder(32);
     	result.setValueExpression("itemLabel", createValueExpressionFromBinding(binding, displayBinding, true, null, String.class));
    	result.setValueExpression("itemValue", createValueExpressionFromBinding(null, binding, false, null, BeanMapAdapter.class));
    	result.setConverter(new AssociationAutoCompleteConverter());
    	result.setScrollHeight(200);
    	
    	expression.setLength(0);
    	expression.append("#{").append(managedBeanName).append(".complete}");
    	result.setCompleteMethod(ef.createMethodExpression(elc, expression.toString(), List.class, new Class[] {String.class}));
    	
    	Map<String, Object> attributes = result.getAttributes();
    	attributes.put("module", query.getOwningModule().getName());
    	attributes.put("query", query.getName());
    	attributes.put("display", displayBinding);
    	
    	addSize(result, null, pixelWidth, null, null, null, Boolean.TRUE);
    	
    	return result;
    }

    public FileUpload fileUpload(String bindingPrefix,
									String binding,
                                    String title,
                                    boolean required,
                                    String disabled) {
        return (FileUpload) input(FileUpload.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
    }

    // this has a customisable toolbar for rich and html wildcat editors.
    public Editor editor(String bindingPrefix,
							String binding,
				            String title,
				            boolean required,
				            String disabled) {
        return (Editor) input(Editor.COMPONENT_TYPE, bindingPrefix, binding, title, required, disabled);
    }
    
    public DataTable dataTable(String binding,
    									String title,
    									String invisible,
    									boolean newButton,
    									boolean clickToNavigate) {
    	DataTable result = (DataTable) a.createComponent(DataTable.COMPONENT_TYPE);
        setId(result);
        addInvisible(result, invisible, null);

        result.setVar(binding);
        result.setValueExpression("value", createValueExpressionFromBinding(binding, true, null, List.class));

        if (clickToNavigate) {
        	String id = result.getId();
	        result.setWidgetVar(id);
	        result.setSelectionMode("single");
	        result.setValueExpression("rowKey", createValueExpressionFromBinding(result.getVar(), Bean.DOCUMENT_ID, true, null, String.class));

	        AjaxBehavior ajax = (AjaxBehavior) a.createBehavior(AjaxBehavior.BEHAVIOR_ID);
			StringBuilder expression = new StringBuilder(64);
			expression.append("#{").append(managedBeanName).append('.').append(ImplicitActionName.Navigate.name().toLowerCase()).append('}');
	        MethodExpression me = ef.createMethodExpression(elc, expression.toString(), null, new Class[0]);
			ajax.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(me, me));
	        result.addClientBehavior("rowSelect", ajax);
        }
        
        UIOutput heading = (UIOutput) a.createComponent(UIOutput.COMPONENT_TYPE);
        heading.setValue(title);
        if (newButton) {
	        CommandButton button = actionButton("New",
													"New Record",
													ImplicitActionName.Add,
													null,
													binding,
													null,
													null,
													Boolean.TRUE,
													null,
													null);
//	        button.setStyle("float:left");
//	        button.setStyle("position:absolute;top:5px;left:5px");
	        OutputPanel headingPanel = (OutputPanel) a.createComponent(OutputPanel.COMPONENT_TYPE);
//	        headingPanel.setStyle("width:100%;height:40px");
	        headingPanel.getChildren().add(heading);
	        headingPanel.getChildren().add(button);
	        result.getFacets().put("header", headingPanel);
        }
        else {
	        result.getFacets().put("header", heading);
        }
        
        return result;
    }

    public Column dataTableActionColumn(String listBinding, boolean mobile) {
		Column result = column(null,
								null,
								"Actions",
				                HorizontalAlignment.centre,
				                true,
				                Integer.valueOf(75));
		CommandLink link = actionLink(mobile ? "" : "Edit",
										"Edit the record",
										ImplicitActionName.Navigate,
										null,
										listBinding,
										null,
										null,
										Boolean.TRUE,
										null,
										null);
/*
        HtmlOutputLink l = outputLink("./", null, null, null);
		HtmlOutputText t = text("Edit", null);

<p:column headerText="Actions" style="width:75px">
<h:outputLink value="./">
<h:outputText value="Edit" />
<f:param name="a" value="#{WebAction.e.toString()}" />
<f:param name="f" value="t" />
<f:param name="m" value="#{row['bizModule']}" />
<f:param name="d" value="#{row['bizDocument']}" />
<f:param name="i" value="#{row['bizId']}" />
</h:outputLink>
</p:column>
*/
        result.getChildren().add(link);
        
        return result;
    }
    
    public CellEditor cellEditor(String listBinding, String binding) {
    	CellEditor result = (CellEditor) a.createComponent(CellEditor.COMPONENT_TYPE);

    	String expression = null;
    	if (binding == null) {
    		expression = new StringBuilder(8).append('{').append(Bean.BIZ_KEY).append('}').toString();
    	}
    	else {
        	expression = new StringBuilder(binding.length() + 2).append('{').append(binding).append('}').toString();
    	}
        ValueExpression ve = createValueExpressionFromBinding(listBinding, expression, true, null, String.class);
        UIOutput text = new UIOutput();
        text.setValueExpression("value", ve);
        result.getFacets().put("output", text);
        
        return result;
    }
    
	public DataList dataList(String binding, String title, String invisible) {
		DataList result = (DataList) a.createComponent(DataList.COMPONENT_TYPE);
		setId(result);
		result.setValueExpression("pt:data-inset", createValueExpressionFromCondition("true", null));
		addInvisible(result, invisible, null);
		
        UIOutput heading = (UIOutput) a.createComponent(UIOutput.COMPONENT_TYPE);
        heading.setValue(title);
        result.getFacets().put("header", heading);

		result.setVar(binding);
		result.setValueExpression("value", createValueExpressionFromBinding(binding, true, null, List.class));

		return result;
	}

    public AccordionPanel accordionPanel(String invisible) {
        AccordionPanel result = (AccordionPanel) a.createComponent(AccordionPanel.COMPONENT_TYPE);
        setId(result);
        addInvisible(result, invisible, null);
        return result;
    }

    public Column column(String listBinding,
    						String sortBinding,
    						String title, 
    						HorizontalAlignment alignment,
    						boolean noWrap,
    						Integer pixelWidth) {
        Column result = (Column) a.createComponent(Column.COMPONENT_TYPE);
        setId(result);

        result.setHeaderText(title);
        if (sortBinding != null) {
        	result.setValueExpression("sortBy", createValueExpressionFromBinding(listBinding, sortBinding, true, null, Object.class));
        }

        StringBuilder style = new StringBuilder(64);
        if (pixelWidth != null) {
        	style.append("width:").append(pixelWidth).append("px;");
        }
        if (noWrap) {
            style.append("white-space:nowrap;");
        }
        if ((alignment != null) && (! HorizontalAlignment.left.equals(alignment))) {
            style.append("text-align:").append(alignment).append(';');
        }
        if (style.length() > 0) {
            result.setStyle(style.toString());
        }

        return result;
    }

	public UISelectItems selectItems(String listBinding, String binding, boolean includeEmptyItems) {
		UISelectItems result = (UISelectItems) a.createComponent(UISelectItems.COMPONENT_TYPE);
		setId(result);
		StringBuilder expression = new StringBuilder(32);
		expression.append("getSelectItems('").append(binding).append("',").append(includeEmptyItems).append(')');
		ValueExpression valueExpression = null;
		if (listBinding != null) {
			valueExpression = createValueExpressionFromBinding(listBinding, expression.toString(), false, null, List.class);
		}
		else {
			valueExpression = createValueExpressionFromBinding(expression.toString(), false, null, List.class);
		}
		result.setValueExpression("value", valueExpression);

		return result;
	}

    private UIInput input(String componentType,
    								String listBinding,
									String binding,
									String title,
									boolean required,
									String disabled) {
		UIInput result = (UIInput) a.createComponent(componentType);
		setId(result);
		if (listBinding != null) {
			result.setValueExpression("value", createValueExpressionFromBinding(listBinding, binding, true, null, Object.class));
		}
		else {
			result.setValueExpression("value", createValueExpressionFromBinding(binding, true, null, Object.class));
		}
		result.setValueExpression("title", ef.createValueExpression(elc,
		                                                                required ? title + " *" : title,
		                                                                String.class));

// Cannot utilise the faces required attributes as some requests need to ignore required-ness.
// eg - triggered actions on widget events.
// Setting required attribute to an expression worked server-side but the client-side message integration didn't.
//		result.setValueExpression("required", ef.createValueExpression(required ? "#{true}" : "false",
//																	    Boolean.class));
//		result.setRequiredMessage(title + " is required");
		addDisabled(result, disabled);
		
		return result;
	}
	
    private ValueExpression createValueExpressionFromBinding(String binding,
    															boolean map,
    															String extraELConditionToAppend,
                                                            	Class<?> typeReturned) {
    	StringBuilder expressionPrefix = new StringBuilder(64);
    	expressionPrefix.append(managedBeanName).append(".currentBean");
    	String fullBinding = binding;

    	return createValueExpressionFromBinding(expressionPrefix.toString(), fullBinding, map, extraELConditionToAppend, typeReturned);
    }
    
    private ValueExpression createValueExpressionFromBinding(String expressionPrefix, 
	                                                            String binding,
	                                                            boolean map,
	                                                            String extraELConditionToAppend,
	                                                            Class<?> typeReturned) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("#{");
        if (expressionPrefix != null) {
        	if (map) {
        		sb.append(expressionPrefix).append("['");
        	}
        	else {
        		sb.append(expressionPrefix).append('.');
        	}
        }
        sb.append(binding);
        if (map && (expressionPrefix != null)) {
        	sb.append("']");
        }
        if (extraELConditionToAppend != null) {
        	sb.append(" and ").append(extraELConditionToAppend);
        }
        sb.append('}');
//System.out.println("EL = " + sb);
        return ef.createValueExpression(elc, sb.toString(), typeReturned);
    }
	
    private ValueExpression createValueExpressionFromCondition(String condition, String extraELConditionToAppend) {
    	if ("false".equals(condition)) {
    		return ef.createValueExpression(condition, Boolean.class);
    	}
    	else if ("true".equals(condition)) {
    		if (extraELConditionToAppend == null) {
    	 		return ef.createValueExpression(condition, Boolean.class);
    		}
    		
			return createValueExpressionFromBinding(null, extraELConditionToAppend, false, null, Boolean.class);
    	}
    	
    	return createValueExpressionFromBinding(condition, true, extraELConditionToAppend, Boolean.class);
    }

	private void addDisabled(UIComponent component,
                                        String disabled) {
	    if (disabled != null) {
	        component.setValueExpression("disabled", createValueExpressionFromCondition(disabled, null));
	    }
	}
	
	private void addInvisible(UIComponent component,
								String invisible,
								String extraELToAppend) {
	    if (invisible != null) {
	        String visible = BindUtil.negateCondition(invisible);
            component.setValueExpression("rendered", createValueExpressionFromCondition(visible, extraELToAppend));
	    }
	}
	
	private void addSize(UIComponent component,
							String existingStyle,
                            Integer pixelWidth, 
                            Integer percentageWidth,
                            Integer pixelHeight,
                            Integer percentageHeight,
                            Boolean defaultTo100InsteadOf98Percent) {
		StringBuilder style = new StringBuilder(64);
		boolean noWidth = true;
		if (existingStyle != null) {
			style.append(existingStyle);
		}
	    if (pixelWidth != null) {
	    	noWidth = false;
	    	style.append("width:").append(pixelWidth).append("px");
	    }
	    else if (percentageWidth != null) {
	    	noWidth = false;
            style.append("width:").append(percentageWidth).append('%');
	    }
	    if (noWidth && (defaultTo100InsteadOf98Percent != null)) {
	    	style.append(defaultTo100InsteadOf98Percent.booleanValue() ? "width:100%" : "width:98%");
	    }
	    if (pixelHeight != null) {
	        if (style.length() > 0) {
	            style.append(';');
	        }
	        style.append("height:").append(pixelHeight).append("px");
	    }
	    else if (percentageHeight != null) {
            if (style.length() > 0) {
                style.append(';');
            }
            style.append("height:").append(percentageHeight).append("%");
	    }
        component.setValueExpression("style", ef.createValueExpression(style.toString(), String.class));
	}
}