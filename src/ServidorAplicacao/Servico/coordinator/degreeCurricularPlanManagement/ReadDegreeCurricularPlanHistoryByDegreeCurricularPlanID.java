
package ServidorAplicacao.Servico.coordinator.degreeCurricularPlanManagement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import pt.utl.ist.berserk.logic.serviceManager.IService;
import DataBeans.InfoCurricularCourse;
import DataBeans.InfoCurricularCourseScopeWithCurricularCourseAndBranchAndSemesterAndYear;
import DataBeans.InfoDegreeCurricularPlan;
import Dominio.DegreeCurricularPlan;
import Dominio.ICurricularCourse;
import Dominio.ICurricularCourseScope;
import Dominio.IDegreeCurricularPlan;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;

/**
 * 
 * @author  <a href="mailto:amam@mega.ist.utl.pt">Amin Amirali</a>
 * @author  <a href="mailto:frnp@mega.ist.utl.pt">Francisco Paulo</a>
 * 
 */
public class ReadDegreeCurricularPlanHistoryByDegreeCurricularPlanID implements IService {
    public InfoDegreeCurricularPlan run(Integer degreeCurricularPlanID) throws FenixServiceException {

        InfoDegreeCurricularPlan infoDegreeCurricularPlan = null;
        try {
            ISuportePersistente sp = SuportePersistenteOJB.getInstance();

            IDegreeCurricularPlan degreeCurricularPlan = (IDegreeCurricularPlan) sp
                    .getIPersistentDegreeCurricularPlan().readByOID(DegreeCurricularPlan.class,
                            degreeCurricularPlanID);

            if (degreeCurricularPlan != null) {
                List allCurricularCourses = sp.getIPersistentCurricularCourse()
                        .readCurricularCoursesByDegreeCurricularPlan(degreeCurricularPlan);

                if (allCurricularCourses != null && !allCurricularCourses.isEmpty()) {

                    Iterator iterator = allCurricularCourses.iterator();
                    while (iterator.hasNext()) {
                        ICurricularCourse curricularCourse = (ICurricularCourse) iterator.next();

                        List curricularCourseScopes = sp.getIPersistentCurricularCourseScope()
                                .readByCurricularCourse(curricularCourse);

                        if (curricularCourseScopes != null) {
                            curricularCourse.setScopes(curricularCourseScopes);
                        }
                    }

                    infoDegreeCurricularPlan = createInfoDegreeCurricularPlan(degreeCurricularPlan,
                            allCurricularCourses);
                }
            }
        } catch (ExcepcaoPersistencia e) {
            throw new FenixServiceException(e);
        }
        return infoDegreeCurricularPlan;
    }

    private InfoDegreeCurricularPlan createInfoDegreeCurricularPlan(
            IDegreeCurricularPlan degreeCurricularPlan, List allCurricularCourses) {

        InfoDegreeCurricularPlan infoDegreeCurricularPlan = InfoDegreeCurricularPlan
                .newInfoFromDomain(degreeCurricularPlan);

        List allInfoCurricularCourses = new ArrayList();

        CollectionUtils.collect(allCurricularCourses, new Transformer() {
            public Object transform(Object arg0) {
                ICurricularCourse curricularCourse = (ICurricularCourse) arg0;
                List allInfoCurricularCourseScopes = new ArrayList();
                CollectionUtils.collect(curricularCourse.getScopes(), new Transformer() {
                    public Object transform(Object arg0) {
                        ICurricularCourseScope curricularCourseScope = (ICurricularCourseScope) arg0;

                        return InfoCurricularCourseScopeWithCurricularCourseAndBranchAndSemesterAndYear
                                .newInfoFromDomain(curricularCourseScope);
                    }
                }, allInfoCurricularCourseScopes);

                InfoCurricularCourse infoCurricularCourse = InfoCurricularCourse
                        .newInfoFromDomain(curricularCourse);
                infoCurricularCourse.setInfoScopes(allInfoCurricularCourseScopes);
                return infoCurricularCourse;
            }
        }, allInfoCurricularCourses);

        infoDegreeCurricularPlan.setCurricularCourses(allInfoCurricularCourses);
        return infoDegreeCurricularPlan;
    }
}