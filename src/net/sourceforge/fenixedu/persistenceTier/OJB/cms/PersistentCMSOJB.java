/**
 * 
 */


package net.sourceforge.fenixedu.persistenceTier.OJB.cms;


import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sourceforge.fenixedu.domain.cms.Cms;
import net.sourceforge.fenixedu.domain.cms.ICms;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.OJB.PersistentObjectOJB;
import net.sourceforge.fenixedu.persistenceTier.cms.IPersistentCMS;
import net.sourceforge.fenixedu.persistenceTier.versionedObjects.cms.CMSVO;

import org.apache.ojb.broker.query.Criteria;

/**
 * @author <a href="mailto:goncalo@ist.utl.pt">Goncalo Luiz</a> <br/> <br/>
 *         <br/> Created on 12:18:09,25/Out/2005
 * @version $Id$
 */
public class PersistentCMSOJB extends PersistentObjectOJB implements IPersistentCMS
{
	private final static String propertiesFilename = "/cms.properties";

	private final static String defaultFenixCMSName = "FenixCMS";

	private static Properties properties;

	public ICms readFenixCMS() throws ExcepcaoPersistencia
	{
		String fenixCMSName = PersistentCMSOJB.defaultFenixCMSName;
		if (PersistentCMSOJB.properties == null)
		{
			this.properties = new Properties();
			try
			{
				InputStream inputStream = getClass().getResourceAsStream(this.propertiesFilename);
				properties.load(inputStream);
				fenixCMSName = this.properties.getProperty("server.url");
				if (fenixCMSName == null)
				{
					fenixCMSName = PersistentCMSOJB.defaultFenixCMSName;
				}
			}

			catch (Exception e)
			{
				// the default server
				fenixCMSName = PersistentCMSOJB.defaultFenixCMSName;
			}
		}

		Criteria criteria = new Criteria();
		criteria.addEqualTo("name", fenixCMSName);
		return (ICms) queryObject(Cms.class, criteria);
	}

	// public List readByExecutionYear(String executionYear) throws
	// ExcepcaoPersistencia {
	// Criteria criteria = new Criteria();
	// criteria.addEqualTo("executionYear.year", executionYear);
	// return queryList(ExecutionDegree.class, criteria,
	// "KEY_DEGREE_CURRICULAR_PLAN", true);
	// }
	// }
}
