package org.skyve.metadata.view.model.map;

import java.util.ArrayList;
import java.util.List;

import org.skyve.CORE;
import org.skyve.domain.Bean;
import org.skyve.metadata.module.query.Query;
import org.skyve.persistence.DocumentQuery;
import org.skyve.persistence.Persistence;

import com.vividsolutions.jts.geom.Envelope;

public class DefaultQueryMapModel<T extends Bean> extends DefaultMapModel<T> {
	/**
	 * For Serialization
	 */
	private static final long serialVersionUID = 5182580858481923068L;

	private Query query;
	private DocumentQuery documentQuery; // from query

	public DefaultQueryMapModel(Query query) {
		this.query = query;
	}

	@Override
	public MapResult getResult(Envelope mapExtents) throws Exception {
		Persistence persistence = CORE.getPersistence();

		if (documentQuery == null) {
			documentQuery = query.constructDocumentQuery(null, null);
		}
		
		List<MapItem> items = new ArrayList<>(256);
		for (Bean bean : persistence.iterate(documentQuery)) {
			addItem(bean, items, mapExtents);
		}
		
		return new MapResult(items, null);
	}
}