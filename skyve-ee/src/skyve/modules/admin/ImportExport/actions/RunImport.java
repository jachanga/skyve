package modules.admin.ImportExport.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.skyve.CORE;
import org.skyve.domain.PersistentBean;
import org.skyve.domain.messages.Message;
import org.skyve.domain.messages.MessageSeverity;
import org.skyve.domain.messages.OptimisticLockException;
import org.skyve.domain.messages.UploadException;
import org.skyve.domain.messages.ValidationException;
import org.skyve.impl.bizport.POISheetLoader;
import org.skyve.metadata.controller.ServerSideAction;
import org.skyve.metadata.controller.ServerSideActionResult;
import org.skyve.persistence.Persistence;
import org.skyve.util.Util;
import org.skyve.web.WebContext;

import modules.admin.ImportExportColumn.ImportExportColumnBizlet;
import modules.admin.domain.ImportExport;
import modules.admin.domain.ImportExportColumn;

public class RunImport implements ServerSideAction<ImportExport> {

	private static final long serialVersionUID = 7301976416286938546L;

	@Override
	public ServerSideActionResult<ImportExport> execute(ImportExport bean, WebContext webContext)
			throws Exception {

		if (bean.getImportFileAbsolutePath() != null) {

			File importFile = new File(bean.getImportFileAbsolutePath());
			UploadException exception = new UploadException();

			int i = 0;
			try (InputStream poiStream = new FileInputStream(importFile)) {

				POISheetLoader loader = new POISheetLoader(poiStream, 0, bean.getModuleName(), bean.getDocumentName(), exception);
				loader.setDebugMode(true);

				if (Boolean.TRUE.equals(bean.getFileContainsHeaders())) {
					loader.setDataIndex(1);
				}

				// prepare debug message
				StringBuilder sb = new StringBuilder(64);
				sb.append("Bean ");

				// and field bindings to loader
				for (ImportExportColumn col : bean.getImportExportColumns()) {
					sb.append(" ");
					if (Boolean.TRUE.equals(bean.getAdvancedMode()) || ImportExportColumnBizlet.ADVANCED.equals(col.getBindingName())) {
						if (col.getBindingExpression() != null) {
							// Util.LOGGER.info("adding " + col.getBindingExpression());
							loader.addField(col.getBindingExpression());

							// prepare debug
							sb.append(col.getBindingExpression()).append("=`").append(col.getBindingExpression()).append("`");
						} else {
							loader.addField((String) null);
						}
					} else {
						if (col.getBindingName() != null) {
							// Util.LOGGER.info("adding " + col.getBindingName());
							loader.addField(col.getBindingName());

							// prepare debug
							sb.append(col.getBindingName()).append("=`{").append(col.getBindingName()).append("}`");
						} else {
							loader.addField((String) null);
						}
					}

				}

				// get results from sheet
				// List<PersistentBean> beans = loader.beanResults();

				// save them
				int saveIndex = 1;
				if (Boolean.TRUE.equals(bean.getFileContainsHeaders())) {
					saveIndex++;
				}

				Util.LOGGER.info("Loading");
				Persistence persistence = CORE.getPersistence();
				while (loader.hasNextData()) {
					loader.nextData();
					if (loader.isNoData()) {
						Util.LOGGER.info("End of import found at " + loader.getWhere());
						break;
					}

					PersistentBean b = loader.beanResult();
					if(b==null) {
						Util.LOGGER.info("Loaded failed at " + loader.getWhere());
					} else {
						Util.LOGGER.info(b.getBizKey() + " - Loaded successfully");
					}
					
					try {
						if (b != null && (b.getBizKey() == null || b.getBizKey().trim().length() == 0)) {
							String msg = "The new record has no value for bizKey at row " + saveIndex + ".";
							ValidationException ve = new ValidationException(new Message(msg));
							throw ve;
						}
						b = persistence.save(b);
						Util.LOGGER.info(b.getBizKey() + " - Saved successfully");
						persistence.evictCached(b);
						
//						persistence.commit(false);
//						persistence.begin();
					} catch (ValidationException ve) {
						StringBuilder msg = new StringBuilder();
						msg.append("The import succeeded but the imported record could not be saved because imported values were not valid:");
						msg.append("\nCheck upload values and try again.");
						msg.append("\n");
						for (Message m : ve.getMessages()) {
							msg.append("\n").append(m.getErrorMessage());
						}
						
						throw new ValidationException(new Message(msg.toString()));
					} catch (OptimisticLockException ole) {
						StringBuilder msg = new StringBuilder();
						msg.append("The import succeeded but the save failed.");
						msg.append(
								"\nCheck that you don't have duplicates in your file, or multiple rows in your file are finding the same related record, or that other users are not changing related data.");
						throw new ValidationException(new Message(msg.toString()));
					} catch (Exception e) {
						StringBuilder msg = new StringBuilder();
						msg.append("The import succeeded but saving the records failed.");
						msg.append("\nCheck that you are uploading to the correct binding and that you have supplied enough information for the results to be saved.");
						throw new ValidationException(new Message(msg.toString()));
					}
					i++;
					saveIndex++;

				}
			}

			// construct result message
			StringBuilder sb = new StringBuilder();
			if (i > 0) {
				sb.append("Successfully created ").append(i).append(" records");
			} else {
				sb.append("Import unsuccessful. Try again.");
			}
			bean.setResults(sb.toString());
			webContext.growl(MessageSeverity.info, sb.toString());
		}

		return new ServerSideActionResult<>(bean);
	}
}
