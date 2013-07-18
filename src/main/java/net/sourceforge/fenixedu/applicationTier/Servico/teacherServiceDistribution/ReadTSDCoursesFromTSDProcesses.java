package net.sourceforge.fenixedu.applicationTier.Servico.teacherServiceDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sourceforge.fenixedu.applicationTier.Filtro.DepartmentMemberAuthorizationFilter;
import net.sourceforge.fenixedu.applicationTier.Filtro.EmployeeAuthorizationFilter;
import net.sourceforge.fenixedu.applicationTier.Filtro.TeacherAuthorizationFilter;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.NotAuthorizedException;
import net.sourceforge.fenixedu.dataTransferObject.teacherServiceDistribution.TSDCourseDTOEntry;
import net.sourceforge.fenixedu.domain.ExecutionSemester;
import net.sourceforge.fenixedu.domain.RootDomainObject;
import net.sourceforge.fenixedu.domain.teacherServiceDistribution.TSDCourse;
import net.sourceforge.fenixedu.domain.teacherServiceDistribution.TeacherServiceDistribution;
import pt.ist.fenixWebFramework.services.Service;
import pt.utl.ist.fenix.tools.util.Pair;

public class ReadTSDCoursesFromTSDProcesses {
    public List<TSDCourseDTOEntry> run(Map<Integer, Pair<Integer, Integer>> tsdProcessIdMap) {
        List<TSDCourseDTOEntry> tsdCourseDTOEntryList = new ArrayList<TSDCourseDTOEntry>();

        for (Integer tsdProcessPhaseId : tsdProcessIdMap.keySet()) {
            TeacherServiceDistribution tsd = null;

            tsd = RootDomainObject.getInstance().readTeacherServiceDistributionByOID(tsdProcessIdMap.get(tsdProcessPhaseId).getKey());

            List<ExecutionSemester> executionPeriodList =
                    getExecutionPeriodList(tsd, tsdProcessIdMap.get(tsdProcessPhaseId).getValue());

            List<TSDCourse> tsdCourseList =
                    new ArrayList<TSDCourse>(tsd.getActiveTSDCourseByExecutionPeriods(executionPeriodList));

            for (TSDCourse tsdCourse : tsdCourseList) {
                tsdCourseDTOEntryList.add(new TSDCourseDTOEntry(tsdCourse, executionPeriodList));
            }
        }

        return tsdCourseDTOEntryList;
    }

    private List<ExecutionSemester> getExecutionPeriodList(TeacherServiceDistribution tsd, Integer executionPeriodId) {
        List<ExecutionSemester> executionPeriodList = new ArrayList<ExecutionSemester>();

        ExecutionSemester executionSemester = RootDomainObject.getInstance().readExecutionSemesterByOID(executionPeriodId);

        if (executionSemester != null) {
            executionPeriodList.add(executionSemester);
        } else {
            executionPeriodList.addAll(tsd.getTSDProcessPhase().getTSDProcess().getExecutionPeriods());
        }

        return executionPeriodList;
    }

    // Service Invokers migrated from Berserk

    private static final ReadTSDCoursesFromTSDProcesses serviceInstance = new ReadTSDCoursesFromTSDProcesses();

    @Service
    public static List<TSDCourseDTOEntry> runReadTSDCoursesFromTSDProcesses(Map<Integer, Pair<Integer, Integer>> tsdProcessIdMap)
            throws NotAuthorizedException {
        try {
            DepartmentMemberAuthorizationFilter.instance.execute();
            return serviceInstance.run(tsdProcessIdMap);
        } catch (NotAuthorizedException ex1) {
            try {
                TeacherAuthorizationFilter.instance.execute();
                return serviceInstance.run(tsdProcessIdMap);
            } catch (NotAuthorizedException ex2) {
                try {
                    EmployeeAuthorizationFilter.instance.execute();
                    return serviceInstance.run(tsdProcessIdMap);
                } catch (NotAuthorizedException ex3) {
                    throw ex3;
                }
            }
        }
    }

}