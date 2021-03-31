/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.type.DateType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.model.RWikiCurrentObjectImpl;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiCurrentObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiHistoryObjectDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.RWikiObjectContentDao;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiCurrentObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiHistoryObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

// FIXME: Component
@Slf4j
public class RWikiCurrentObjectDaoImpl extends HibernateDaoSupport implements RWikiCurrentObjectDao, ObjectProxy {

	protected RWikiObjectContentDao contentDAO = null;

	protected RWikiHistoryObjectDao historyDAO = null;

	public boolean exists(final String name) {
		long start = System.currentTimeMillis();
		try {
			HibernateCallback<Number> callback = session -> (Number) session
                    .createQuery("select count(*) from RWikiCurrentObjectImpl r where r.name = :name")
                    .setString("name", name)
                    .uniqueResult();

			Integer count = getHibernateTemplate().execute(callback).intValue();

			return (count > 0);
		} finally {
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.exists: " + name, start, finish);
		}
	}

	public RWikiCurrentObject findByGlobalName(final String name) {
		long start = System.currentTimeMillis();
		try {
			// there is no point in sorting by version, since there is only one
			// version in
			// this table.
			// also using like is much slower than eq
			HibernateCallback<List> callback = session -> session
					.createCriteria(RWikiCurrentObject.class)
                    .add(Expression.eq("name", name))
					.list();
			List found = getHibernateTemplate().execute(callback);
			if (found.size() == 0)
			{
				log.debug("Found {} objects with name {}", found.size(), name);
				return null;
			}
			log.debug("Found {} objects with name {} returning most recent one.", found.size(), name);
			return (RWikiCurrentObject) proxyObject(found.get(0));
		} finally {
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.findByGlobalName: " + name, start, finish);
		}
	}

	public List findByGlobalNameAndContents(final String criteria, final String user, final String realm) {

		String[] criterias = criteria.split("\\s\\s*");

		final StringBuffer expression = new StringBuffer();
		final List criteriaList = new ArrayList();

/*
		criteriaList.add(realm);
		criteriaList.add("%" + criteria.toLowerCase() + "%");
		criteriaList.add("%" + criteria.toLowerCase() + "%");

		// WARNING: In MySQL like does not produce a case sensitive search so
		// this is Ok
		// Oracle can probaly do it, but would need some set up (maybee)
		// http://asktom.oracle.com/pls/ask/f?p=4950:8:::::F4950_P8_DISPLAYID:16370675423662

		for (int i = 0; i < criterias.length; i++)
		{
			if (!"".equals(criterias[i]))
			{
				expression.append(" or lower(c.content) like ? ");
				criteriaList.add("%" + criterias[i].toLowerCase() + "%");
			}
		}
		if ("".equals(criteria))
		{
			expression.append(" or lower(c.content) like ? ");
			criteriaList.add("%%");
		}
		final Type[] types = new Type[criteriaList.size()];
		for (int i = 0; i < types.length; i++)
		{
			types[i] = StringType.INSTANCE;
		}
*/


	/*
	 *   JJ search
	 */

		// ((!)*(\S+)\s+and\s+(!)*(\S+))*(!)*(\S+)*\s*
		// ((!)?(\S+)\s+and\s+(!)?(\S+))*(!)?(\S+)*\s*


		//(!)?(\S+)\s+and\s+(!)?(\S+))*(\1*and\s+(\S+))*(!)?(\S+)*\s*
		// ((!)?(\S+)\s+and\s+(!)?(\S+))*(\1?\s+(and\s+(\S+))*)*(!)?(\S+)*\s*
		// (?<and>(!)?(\S+)\s+and\s+(!)?(\S+))*((?P=and)*\s+and\s+(\S+))*(!)?(\S+)*\s*


		// (?<test>\S+ and \S+)*(?<s>(?(R&test)| and \S+))?

		// (?<and>((!)?(\S+)\s+and\s+(!)?(\S+)))*((?(R&and)|(\s+and\s+(\S+))))?((?(7)(\s+and\s+(\S+)(*ACCEPT))*|\s+and\s+(\S+)))(!)?(\S+)*\s*


		//(?<and>(!)?(\S+)\s+and\s+(!)?(\S+))*((?P=and)*\s+and\s+(\S+)(*ACCEPT))*(!)?(\S+)*\s*   ONLY PERL conform
		 // (?<and>(!)?(\S+)\s+and\s+(!)?(\S+))*(\k<and>*\s+and\s+(!)?(\S+))*(!)?(\S+)*\s*


		// (?<and>(!)?(\S+)\s+and\s+(!)?(\S+))*(?<and2>\k<and>*\s+and\s+(!)?(\S+)\s+[^and])*(\k<and2>*(?<!\A)and\s+(!)?(\S+))*(!)?(\S+)*(\s*)
		// (?<and>(!)?(\S+)\s+and\s+(!)?(\S+))*(?<and2>\k<and>*\s+and\s+(!)?(\S+)\s+[^and])*(\k<and2>*(?<!\A[^\s+and])and\s+(!)?(\S+))*(!)?(\S+)*\s*

		// (\A\s*and)*(?<and>(!)?(\S+)\s+and\s+(!)?(\S+))*(?<and2>\k<and>*\s+and\s+(!)?(\S+)\s+[^and])*(\k<and2>*(?<!\A[^\s*and])and\s+(!)?(\S+))*(!)?(\S+)*\s*



		// capture 1 is needed for regex to work but is ignored for now!!
		Pattern idPattern = Pattern.compile("(\\A\\s*and)*(?<and>(!)?(\\S+)\\s+and\\s+(!)?(\\S+))*(\\k<and>*and\\s+(!)?(\\S+))*(!)?(\\S+)*\\s*",Pattern.CASE_INSENSITIVE);    // ((!)?(\S+)\s+and\s+(!)?(\S+))*(!)?(\S+)*\s*
		Matcher matcher = idPattern.matcher(criteria);

		criteriaList.add(realm);
		int t = 1;

		Boolean firstParam = true;
		Boolean onlyNotSearch = true;
		Boolean firstNotSearchParam = true;

		final StringBuffer expressionNotOperator = new StringBuffer();
		final List criteriaListTmp = new ArrayList();
		final List criteriaListTmp2 = new ArrayList();
		final List criteriaListTmp3 = new ArrayList();


		final List criteriaListTmp_1 = new ArrayList();
		final List criteriaListTmp2_1 = new ArrayList();
		final List criteriaListTmp3_1 = new ArrayList();
		final StringBuffer expression2 = new StringBuffer();
		final StringBuffer expressionNotOperator_1 = new StringBuffer();

		//criteriaListTmp.add("%" + criteria.toLowerCase() + "%");
		//criteriaListTmp.add("%" + criteria.toLowerCase() + "%");

 		// RWikiCurrentObjectImpl as r  RWikiCurrentObjectContentImpl as c

		String query = "select distinct r from RWikiCurrentObjectImpl as r, RWikiCurrentObjectContentImpl as c where r.realm = ? and ";
		String query2 = "select distinct r from RWikiCurrentObjectImpl as r, RWikiCurrentObjectContentImpl as c where r.realm = ? and ";
		criteriaListTmp.add(realm);




		while(matcher.find()){
			if(!matcher.group(0).isEmpty()){

				//check for and operator linkage (word and anotherWord)
				if(matcher.group("and") != null) {
					//update flags
					if (onlyNotSearch) {
						onlyNotSearch = false;
					}


					// left param --> check for!
					if (matcher.group(3) != null) {
						//check if first
						if(firstParam){
							expression.append("lower(c.content) not like ?");
							expression2.append("lower(r.name) not like ?");
							firstParam = false;
						}else{
							expression.append(" or lower(c.content) not like ?");
							expression2.append(" or lower(r.name) not like ?");
						}
					} else {
						if(firstParam){
							expression.append(" lower(c.content) like ?");
							expression2.append(" lower(r.name) like ?");
							firstParam = false;
						}else {
							expression.append(" or lower(c.content) like ?");
							expression2.append(" or lower(r.name) like ?");
						}
					}

					//  right param --> check for!
					if (matcher.group(5) != null) {
						expression.append(" and lower(c.content) not like ? ");
						expression2.append(" and lower(r.name) not like ? ");
					} else {
						expression.append(" and lower(c.content) like ? ");
						expression2.append(" and lower(r.name) like ? ");
					}
					criteriaListTmp2.add("%" + matcher.group(4).toLowerCase() + "%");
					criteriaListTmp2.add("%" + matcher.group(6).toLowerCase() + "%");

					criteriaListTmp2_1.add("%" + matcher.group(4).toLowerCase() + "%");
					criteriaListTmp2_1.add("%" + matcher.group(6).toLowerCase() + "%");
					t += 2;

				// check for trailing and operator
				}else if(matcher.group(7) != null){
					//check for !
					if(matcher.group(8) != null){
						expression.append(" and lower(c.content) not like ? ");
						expression2.append(" and lower(r.name) not like ? ");
					}else {
						expression.append(" and lower(c.content)  like ? ");
						expression2.append(" and lower(r.name)  like ? ");
					}
					criteriaListTmp2.add("%" + matcher.group(9).toLowerCase() + "%");
					criteriaListTmp2_1.add("%" + matcher.group(9).toLowerCase() + "%");
					t++;


					//check for single search param
				}else if(matcher.group(11) != null) {

					//check for !
					if(matcher.group(10) != null){

						if(firstNotSearchParam){
							expressionNotOperator.append("lower(c.content) not like ? ");
							expressionNotOperator_1.append("lower(r.name) not like ? ");
							criteriaListTmp3.add("%" + matcher.group(11).toLowerCase() + "%");
							criteriaListTmp3_1.add("%" + matcher.group(11).toLowerCase() + "%");
							firstNotSearchParam = false;
						}else {

							expressionNotOperator.append(" or lower(c.content) not like ? ");
							expressionNotOperator_1.append(" or lower(r.name) not like ? ");
							criteriaListTmp3.add("%" + matcher.group(11).toLowerCase() + "%");
							criteriaListTmp3_1.add("%" + matcher.group(11).toLowerCase() + "%");
						}

					}else{
						if(onlyNotSearch){
							onlyNotSearch = false;
						}
						if(firstParam){
							expression.append("  lower(c.content) like ? ");
							expression2.append("  lower(r.name) like ? ");
							firstParam = false;
						}else {
							expression.append(" or lower(c.content) like ? ");
							expression2.append(" or lower(r.name) like ? ");
						}
						criteriaListTmp2.add("%" + matcher.group(11).toLowerCase() + "%");
						criteriaListTmp2_1.add("%" + matcher.group(11).toLowerCase() + "%");
					}
					t++;
				}
			}
		}



		if(!onlyNotSearch){
			query += " ( " + expression.toString();
			criteriaListTmp2.forEach(s -> {criteriaList.add(s);});

			query2 += " ( " + expression2.toString();
			criteriaListTmp2_1.forEach(s -> {criteriaListTmp.add(s);});

			if(!criteriaListTmp3.isEmpty()){
				query += " and ( " + expressionNotOperator.toString() + ")) ";
				criteriaListTmp3.forEach(s -> {criteriaList.add(s);});

				query2 += " and ( " + expressionNotOperator_1.toString() + ")) ";
				criteriaListTmp3_1.forEach(s -> {criteriaListTmp.add(s);});

			}else{
				query += ") ";
				query2 += ") ";
			}

		}else {
			query += "( " + expressionNotOperator.toString() + " )";
			criteriaListTmp3.forEach(s -> {criteriaList.add(s);});



			query2 += "( " + expressionNotOperator_1.toString() + " )";
			criteriaListTmp3_1.forEach(s -> {criteriaListTmp.add(s);});
		}

		query += " and r.id = c.rwikiid order by r.name";
		query2 += " and r.id = c.rwikiid order by r.name";




	//	query += query2;
	//	criteriaListTmp.forEach(s -> {criteriaList.add(s);});



		final Type[] types = new Type[t];
		for (int i = 0; i < t; i++)
		{
			types[i] = StringType.INSTANCE;
		}

		String finalQuery = query;
		HibernateCallback<List> callback = session -> session
				.createQuery(finalQuery)
				.setParameters(criteriaList.toArray(), types)
				.list();
		List list1 = getHibernateTemplate().execute(callback);


		String finalQuery2 = query2;
		HibernateCallback<List> callback2 = session -> session
				.createQuery(finalQuery2)
				.setParameters(criteriaListTmp.toArray(), types)
				.list();
		List list2 = getHibernateTemplate().execute(callback2);


		Iterator it = list2.iterator();
		while(it.hasNext()){
			Object o = it.next();
			if(!list1.contains(o)){
				list1.add(o);
			}
		}

		return new ListProxy(list1, this);

		//return new ListProxy(getHibernateTemplate().execute(callback), this);
	}


	public void update(RWikiCurrentObject rwo, RWikiHistoryObject rwho) {
		// should have already checked
		RWikiCurrentObjectImpl impl = (RWikiCurrentObjectImpl) rwo;
		getHibernateTemplate().saveOrUpdate(impl);
		// update the history
		if (rwho != null) {
			rwho.setRwikiobjectid(impl.getId());
			historyDAO.update(rwho);
		}
		// remember to save the content, and make certain the contentDAO is set
		// first
		impl.setRwikiObjectContentDao(contentDAO);
		impl.getRWikiObjectContent().setRwikiid(rwo.getId());
		contentDAO.update(impl.getRWikiObjectContent());
	}

	public RWikiCurrentObject createRWikiObject(String name, String realm) {

		RWikiCurrentObjectImpl returnable = new RWikiCurrentObjectImpl();
		proxyObject(returnable);
		returnable.setName(name);
		returnable.setRealm(realm);
		returnable.setVersion(new Date());
		returnable.setRevision(Integer.valueOf(0));

		returnable.setContent(Messages.getString("RWikiCurrentObjectDaoImpl.30") + Messages.getString("RWikiCurrentObjectDaoImpl.31"));
		return returnable;
	}

	public List findChangedSince(final Date since, final String realm) {
		HibernateCallback<List> callback = session -> session
				.createCriteria(RWikiCurrentObject.class)
				.add(Expression.ge("version", since))
				.add(Expression.eq("realm", realm))
				.addOrder(Order.desc("version"))
				.list();
		return new ListProxy(getHibernateTemplate().execute(callback), this);
	}

	public List findReferencingPages(final String name) {
		HibernateCallback<List> callback = session -> session
				.createQuery("select r.name " + "from RWikiCurrentObjectImpl r where referenced like :name")
				.setString("name", "%::" + name + "::%")
				.list();
		return new ListProxy(getHibernateTemplate().execute(callback), this);
	}

	public RWikiCurrentObject getRWikiCurrentObject(final RWikiObject reference) {
		long start = System.currentTimeMillis();
		try {
			HibernateCallback<List> callback = session -> session.createCriteria(RWikiCurrentObject.class)
                    .add(Expression.eq("id", reference.getRwikiobjectid()))
					.list();
			List found = getHibernateTemplate().execute(callback);
			if (found.size() == 0) {
				log.debug("Found {} objects with id {}", found.size(), reference.getRwikiobjectid());
				return null;
			}
			log.debug("Found {} objects with id {} returning most recent one.", found.size(), reference.getRwikiobjectid());
			return (RWikiCurrentObject) proxyObject(found.get(0));
		}
		finally {
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiCurrentObjectDaoImpl.getRWikiCurrentObject: " + reference.getName(), start, finish);
		}
	}

	public RWikiObjectContentDao getContentDAO() {
		return contentDAO;
	}

	public void setContentDAO(RWikiObjectContentDao contentDAO) {
		this.contentDAO = contentDAO;
	}

	public Object proxyObject(Object o) {
		if (o != null && o instanceof RWikiCurrentObjectImpl) {
			RWikiCurrentObjectImpl rwCo = (RWikiCurrentObjectImpl) o;
			rwCo.setRwikiObjectContentDao(contentDAO);
		}
		return o;
	}

	public RWikiHistoryObjectDao getHistoryDAO() {
		return historyDAO;
	}

	public void setHistoryDAO(RWikiHistoryObjectDao historyDAO) {
		this.historyDAO = historyDAO;
	}

	public List getAll() {
		HibernateCallback<List> callback = session -> {
            return session.createCriteria(RWikiCurrentObject.class)
                    .addOrder(Order.desc("version"))
					.list();
        };
		return new ListProxy(getHibernateTemplate().execute(callback), this);
	}

	public void updateObject(RWikiObject rwo) {
		getHibernateTemplate().saveOrUpdate(rwo);
	}

	public int getPageCount(final String group) {
		long start = System.currentTimeMillis();
		try
		{
			HibernateCallback<Number> callback = session -> (Number) session
					.createQuery("select count(*) from RWikiCurrentObjectImpl r where r.realm = :realm")
					.setString("realm", group)
					.uniqueResult();

			return getHibernateTemplate().execute(callback).intValue();
		}
		finally
		{
			long finish = System.currentTimeMillis();
			TimeLogger.printTimer("RWikiObjectDaoImpl.getPageCount: " + group, //$NON-NLS-1$
					start, finish);
		}
	}

	public List findRWikiSubPages(final String globalParentPageName) {
		HibernateCallback<List> callback = session -> {
            String search = globalParentPageName.replaceAll("([A%_])", "A$1");
            return session.createQuery("from RWikiCurrentObjectImpl as r where r.name like concat( :search , '%' ) escape 'A' order by name asc")
					.setString("search", search)
					.list();
        };
		return new ListProxy(getHibernateTemplate().execute(callback), this);
	}

	public RWikiObject findLastRWikiSubPage(final String globalParentPageName) {
		HibernateCallback<List> callback = session -> {
            String search = globalParentPageName.replaceAll("([A%_])", "A$1");
            return session.createQuery("from RWikiCurrentObjectImpl as r where r.name like concat( :search , '%' ) escape 'A' order by name desc")
					.setString("search", search)
                    .list();
        };
		List l = getHibernateTemplate().execute(callback);
		if (l == null || l.size() == 0) return null;
		return (RWikiObject) l.get(0);
	}

	public List findAllChangedSince(final Date time, final String basepath) {
		HibernateCallback<List> callback = session -> {
            String search = basepath.replaceAll("([A%_])", "A$1");
            return session
                    .createQuery("from RWikiCurrentObjectImpl as r where r.name like concat( :search , '%' ) escape 'A' and r.version >= :time order by r.version desc, r.name asc")
                    .setString("search", search)
                    .setDate("time", time)
                    .list();
        };
		return new ListProxy(getHibernateTemplate().execute(callback), this);

	}

	public List findAllPageNames() {
		HibernateCallback<List> callback = session -> session
				.createQuery("select r.name " + "from RWikiCurrentObjectImpl  r ")
                .list();
		return getHibernateTemplate().execute(callback);
	}

}
