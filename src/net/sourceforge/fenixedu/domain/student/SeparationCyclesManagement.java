package net.sourceforge.fenixedu.domain.student;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import net.sourceforge.fenixedu.domain.Attends;
import net.sourceforge.fenixedu.domain.CurricularCourse;
import net.sourceforge.fenixedu.domain.DegreeCurricularPlan;
import net.sourceforge.fenixedu.domain.Enrolment;
import net.sourceforge.fenixedu.domain.ExecutionSemester;
import net.sourceforge.fenixedu.domain.ExecutionYear;
import net.sourceforge.fenixedu.domain.IEnrolment;
import net.sourceforge.fenixedu.domain.OptionalEnrolment;
import net.sourceforge.fenixedu.domain.Shift;
import net.sourceforge.fenixedu.domain.StudentCurricularPlan;
import net.sourceforge.fenixedu.domain.accounting.Installment;
import net.sourceforge.fenixedu.domain.accounting.PaymentPlan;
import net.sourceforge.fenixedu.domain.accounting.events.AccountingEventsManager;
import net.sourceforge.fenixedu.domain.accounting.events.gratuity.GratuityEvent;
import net.sourceforge.fenixedu.domain.accounting.events.gratuity.GratuityEventWithPaymentPlan;
import net.sourceforge.fenixedu.domain.accounting.events.gratuity.GratuityExemptionJustificationType;
import net.sourceforge.fenixedu.domain.accounting.events.gratuity.PercentageGratuityExemption;
import net.sourceforge.fenixedu.domain.accounting.events.gratuity.ValueGratuityExemption;
import net.sourceforge.fenixedu.domain.accounting.installments.InstallmentWithMonthlyPenalty;
import net.sourceforge.fenixedu.domain.candidacy.Ingression;
import net.sourceforge.fenixedu.domain.candidacy.StudentCandidacy;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.degreeStructure.Context;
import net.sourceforge.fenixedu.domain.degreeStructure.CourseGroup;
import net.sourceforge.fenixedu.domain.degreeStructure.OptionalCurricularCourse;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.domain.exceptions.DomainExceptionWithInvocationResult;
import net.sourceforge.fenixedu.domain.student.registrationStates.RegistrationState;
import net.sourceforge.fenixedu.domain.student.registrationStates.RegistrationStateType;
import net.sourceforge.fenixedu.domain.studentCurriculum.Credits;
import net.sourceforge.fenixedu.domain.studentCurriculum.CreditsDismissal;
import net.sourceforge.fenixedu.domain.studentCurriculum.CurriculumGroup;
import net.sourceforge.fenixedu.domain.studentCurriculum.CurriculumLine;
import net.sourceforge.fenixedu.domain.studentCurriculum.CurriculumModule;
import net.sourceforge.fenixedu.domain.studentCurriculum.CycleCurriculumGroup;
import net.sourceforge.fenixedu.domain.studentCurriculum.Dismissal;
import net.sourceforge.fenixedu.domain.studentCurriculum.EnrolmentWrapper;
import net.sourceforge.fenixedu.domain.studentCurriculum.Equivalence;
import net.sourceforge.fenixedu.domain.studentCurriculum.ExtraCurriculumGroup;
import net.sourceforge.fenixedu.domain.studentCurriculum.OptionalDismissal;
import net.sourceforge.fenixedu.domain.studentCurriculum.Substitution;
import net.sourceforge.fenixedu.domain.studentCurriculum.TemporarySubstitution;
import net.sourceforge.fenixedu.util.InvocationResult;
import net.sourceforge.fenixedu.util.Money;

import org.joda.time.DateTime;
import org.joda.time.YearMonthDay;

import pt.utl.ist.fenix.tools.util.i18n.Language;

public class SeparationCyclesManagement {

    private static final List<DegreeType> ACCEPTED_DEGREE_TYPES = Arrays.asList(DegreeType.BOLONHA_DEGREE,
	    DegreeType.BOLONHA_INTEGRATED_MASTER_DEGREE);

    private final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("resources.ApplicationResources", Language
	    .getLocale());

    public SeparationCyclesManagement() {
    }

    public Registration separateSecondCycle(final StudentCurricularPlan studentCurricularPlan) {
	checkIfCanSeparateSecondCycle(studentCurricularPlan);
	return createNewSecondCycle(studentCurricularPlan);
    }

    protected void checkIfCanSeparateSecondCycle(final StudentCurricularPlan studentCurricularPlan) {

	if (!studentCurricularPlan.isBolonhaDegree()) {
	    throw new DomainException("error.SeparationCyclesManagement.not.bolonha.degree");
	}

	if (!studentCurricularPlan.isActive() && !studentCurricularPlan.getRegistration().isConcluded()) {
	    throw new DomainException("error.SeparationCyclesManagement.not.active.or.concluded", studentCurricularPlan.getName());
	}

	if (studentCurricularPlan.isConclusionProcessed()) {
	    throw new DomainException("error.SeparationCyclesManagement.conclusion.processed");
	}

	if (!ACCEPTED_DEGREE_TYPES.contains(studentCurricularPlan.getDegreeType())) {
	    throw new DomainException("error.SeparationCyclesManagement.invalid.degreeType");
	}

	final CycleCurriculumGroup secondCycle = studentCurricularPlan.getSecondCycle();
	if (secondCycle == null || !secondCycle.isExternal()) {
	    throw new DomainException("error.SeparationCyclesManagement.invalid.secondCycle");
	}

	final CycleCurriculumGroup firstCycle = studentCurricularPlan.getFirstCycle();
	if (firstCycle == null || !firstCycle.isConcluded()) {
	    throw new DomainException("error.SeparationCyclesManagement.invalid.firstCycle");
	}

	if (studentAlreadyHasNewRegistration(studentCurricularPlan)) {
	    final DegreeCurricularPlan degreeCurricularPlan = secondCycle.getDegreeCurricularPlanOfDegreeModule();
	    throw new DomainException("error.SeparationCyclesManagement.already.has.registration", degreeCurricularPlan.getName());
	}
    }

    private boolean studentAlreadyHasNewRegistration(final StudentCurricularPlan studentCurricularPlan) {
	final Student student = studentCurricularPlan.getRegistration().getStudent();
	return student.hasRegistrationFor(studentCurricularPlan.getSecondCycle().getDegreeCurricularPlanOfDegreeModule());
    }

    protected Registration createNewSecondCycle(final StudentCurricularPlan oldStudentCurricularPlan) {
	final Student student = oldStudentCurricularPlan.getRegistration().getStudent();
	final CycleCurriculumGroup oldSecondCycle = oldStudentCurricularPlan.getSecondCycle();
	final DegreeCurricularPlan degreeCurricularPlan = oldSecondCycle.getDegreeCurricularPlanOfDegreeModule();

	final Registration newRegistration = createRegistration(student, oldStudentCurricularPlan);
	final StudentCurricularPlan newStudentCurricularPlan = createStudentCurricularPlan(newRegistration, degreeCurricularPlan);
	final CycleCurriculumGroup newSecondCycle = newStudentCurricularPlan.getSecondCycle();

	copyCycleCurriculumGroupsInformation(oldSecondCycle, newSecondCycle);
	moveExtraCurriculumGroupInformation(oldStudentCurricularPlan, newStudentCurricularPlan);
	moveExtraAttends(oldStudentCurricularPlan, newStudentCurricularPlan);
	tryRemoveOldSecondCycle(oldSecondCycle);
	markOldRegistrationWithConcludedState(oldStudentCurricularPlan);
	moveGratuityEventsInformation(oldStudentCurricularPlan, newStudentCurricularPlan);
	createAdministrativeOfficeFeeAndInsurance(newStudentCurricularPlan);

	return newRegistration;
    }

    private void moveExtraAttends(final StudentCurricularPlan oldStudentCurricularPlan,
	    final StudentCurricularPlan newStudentCurricularPlan) {

	final Set<Attends> attends = new HashSet<Attends>();
	for (final Attends attend : oldStudentCurricularPlan.getRegistration().getAssociatedAttendsSet()) {
	    if (!belongsTo(oldStudentCurricularPlan, attend)
		    && isToMoveAttendsFrom(oldStudentCurricularPlan, newStudentCurricularPlan, attend)) {
		attends.add(attend);
	    }
	}

	for (final Attends attend : attends) {
	    changeShifts(attend, oldStudentCurricularPlan, newStudentCurricularPlan);
	    attend.setRegistration(newStudentCurricularPlan.getRegistration());
	}
    }

    private void changeShifts(final Attends attend, final StudentCurricularPlan oldStudentCurricularPlan,
	    final StudentCurricularPlan newStudentCurricularPlan) {

	for (final Shift shift : getShiftsToMove(attend, oldStudentCurricularPlan)) {
	    shift.unEnrolStudent(oldStudentCurricularPlan.getRegistration());
	    if (!shift.hasStudents(newStudentCurricularPlan.getRegistration())) {
		shift.addStudents(newStudentCurricularPlan.getRegistration());
	    }
	}
    }

    private List<Shift> getShiftsToMove(final Attends attend, final StudentCurricularPlan oldStudentCurricularPlan) {
	final List<Shift> shifts = new ArrayList<Shift>();
	for (final Shift shift : oldStudentCurricularPlan.getRegistration().getShifts()) {
	    if (attend.isFor(shift.getExecutionCourse())) {
		shifts.add(shift);
	    }
	}
	return shifts;
    }

    private boolean belongsTo(final StudentCurricularPlan studentCurricularPlan, final Attends attend) {
	for (final CurricularCourse curricularCourse : attend.getExecutionCourse().getAssociatedCurricularCoursesSet()) {
	    if (studentCurricularPlan.getDegreeCurricularPlan().hasDegreeModule(curricularCourse)) {
		return true;
	    }
	}
	return false;
    }

    private boolean isToMoveAttendsFrom(final StudentCurricularPlan oldStudentCurricularPlan,
	    final StudentCurricularPlan newStudentCurricularPlan, final Attends attend) {

	if (attend.hasEnrolment()) {
	    return !oldStudentCurricularPlan.hasEnrolments(attend.getEnrolment())
		    && newStudentCurricularPlan.hasEnrolments(attend.getEnrolment());
	}

	return !attend.getExecutionPeriod().isBefore(newStudentCurricularPlan.getStartExecutionPeriod());
    }

    private Registration createRegistration(final Student student, final StudentCurricularPlan sourceStudentCurricularPlan) {

	final CycleCurriculumGroup oldSecondCycle = sourceStudentCurricularPlan.getSecondCycle();
	Registration registration = student.getRegistrationFor(oldSecondCycle.getDegreeCurricularPlanOfDegreeModule());

	if (registration != null) {
	    return registration;
	}

	registration = new Registration(student.getPerson(), student.getNumber());
	registration.setDegree(oldSecondCycle.getDegreeCurricularPlanOfDegreeModule().getDegree());
	registration.setStudentCandidacy(createStudentCandidacy(student, oldSecondCycle));

	registration.setStartDate(getBeginDate(sourceStudentCurricularPlan, getExecutionPeriod()));
	registration.getActiveState().setStateDate(getBeginDate(sourceStudentCurricularPlan, getExecutionPeriod()));

	registration.setSourceRegistration(sourceStudentCurricularPlan.getRegistration());
	registration.getActiveState().setResponsiblePerson(null);
	registration.setRegistrationAgreement(RegistrationAgreement.NORMAL);

	return registration;
    }

    private YearMonthDay getBeginDate(final StudentCurricularPlan sourceStudentCurricularPlan,
	    final ExecutionSemester executionSemester) {
	final YearMonthDay start = sourceStudentCurricularPlan.getStartDateYearMonthDay();
	return executionSemester.getBeginDateYearMonthDay().isBefore(start) ? start : executionSemester
		.getBeginDateYearMonthDay();
    }

    private StudentCandidacy createStudentCandidacy(final Student student, final CycleCurriculumGroup oldSecondCycle) {
	final DegreeCurricularPlan dcp = oldSecondCycle.getDegreeCurricularPlanOfDegreeModule();
	return StudentCandidacy.createStudentCandidacy(dcp.getExecutionDegreeByYear(getExecutionYear()), student.getPerson());
    }

    private StudentCurricularPlan createStudentCurricularPlan(final Registration registration,
	    final DegreeCurricularPlan degreeCurricularPlan) {

	StudentCurricularPlan result = registration.getStudentCurricularPlan(degreeCurricularPlan);
	if (result != null) {
	    return result;
	}

	result = StudentCurricularPlan.createWithEmptyStructure(registration, degreeCurricularPlan, registration.getStartDate());
	// set ingression after create studentcurricularPlan
	registration.setIngression(Ingression.DA1C);

	return result;
    }

    private void copyCycleCurriculumGroupsInformation(final CycleCurriculumGroup oldSecondCycle,
	    final CycleCurriculumGroup newSecondCycle) {
	for (final CurriculumModule curriculumModule : oldSecondCycle.getCurriculumModulesSet()) {
	    if (curriculumModule.isLeaf()) {
		copyCurricumLineInformation((CurriculumLine) curriculumModule, newSecondCycle);
	    } else {
		copyCurriculumGroupsInformation((CurriculumGroup) curriculumModule, newSecondCycle);
	    }
	}
    }

    private void copyCurriculumGroupsInformation(final CurriculumGroup source, final CurriculumGroup parent) {
	final CurriculumGroup destination;
	if (parent.hasChildDegreeModule(source.getDegreeModule())) {
	    destination = (CurriculumGroup) parent.getChildCurriculumModule(source.getDegreeModule());
	} else {
	    destination = new CurriculumGroup(parent, source.getDegreeModule());
	}

	for (final CurriculumModule curriculumModule : source.getCurriculumModulesSet()) {
	    if (curriculumModule.isLeaf()) {
		copyCurricumLineInformation((CurriculumLine) curriculumModule, destination);
	    } else {
		copyCurriculumGroupsInformation((CurriculumGroup) curriculumModule, destination);
	    }
	}
    }

    private void copyCurricumLineInformation(final CurriculumLine curriculumLine, final CurriculumGroup parent) {
	if (curriculumLine.isEnrolment()) {
	    final Enrolment enrolment = (Enrolment) curriculumLine;
	    if (enrolment.getExecutionPeriod().isAfterOrEquals(getExecutionPeriod())) {
		moveEnrolment(enrolment, parent);
	    } else if (enrolment.isApproved()) {
		createSubstitutionForEnrolment((Enrolment) curriculumLine, parent);
	    }
	} else if (curriculumLine.isDismissal()) {
	    createDismissal((Dismissal) curriculumLine, parent);
	} else {
	    throw new DomainException("error.unknown.curriculumLine");
	}
    }

    private void moveEnrolment(final Enrolment enrolment, final CurriculumGroup parent) {
	final CurriculumModule child = parent.getChildCurriculumModule(enrolment.getDegreeModule());
	if (child != null && child.isEnrolment()) {
	    final Enrolment childEnrolment = (Enrolment) child;
	    if (childEnrolment.getExecutionPeriod() == enrolment.getExecutionPeriod()) {
		throw new DomainException("error.SeparationCyclesManagement.enrolment.should.not.exist.for.same.executionPeriod");
	    }
	}

	final StudentCurricularPlan oldStudentCurricularPlan = enrolment.getStudentCurricularPlan();
	final Registration registration = parent.getStudentCurricularPlan().getRegistration();

	enrolment.setCurriculumGroup(parent);

	for (final Attends attend : enrolment.getAttends()) {
	    changeShifts(attend, oldStudentCurricularPlan, parent.getStudentCurricularPlan());
	    attend.setRegistration(registration);
	}
    }

    private void createSubstitutionForEnrolment(final Enrolment enrolment, final CurriculumGroup parent) {
	if (parent.hasChildDegreeModule(enrolment.getDegreeModule())) {
	    return;
	}

	final Substitution substitution = new Substitution();
	substitution.setStudentCurricularPlan(parent.getStudentCurricularPlan());
	substitution.setExecutionPeriod(getExecutionPeriod());
	EnrolmentWrapper.create(substitution, enrolment);

	if (enrolment.isOptional()) {
	    final OptionalEnrolment optional = (OptionalEnrolment) enrolment;
	    if (parent.hasChildDegreeModule(optional.getOptionalCurricularCourse())) {
		return;
	    }
	    createNewOptionalDismissal(substitution, parent, optional.getOptionalCurricularCourse(), optional.getEctsCredits());
	} else {
	    createNewDismissal(substitution, parent, enrolment.getCurricularCourse());
	}
    }

    private Dismissal createNewDismissal(final Credits credits, final CurriculumGroup parent,
	    final CurricularCourse curricularCourse) {

	if (!hasCurricularCourseToDismissal(parent, curricularCourse, getExecutionYear())) {
	    throw new DomainException("error.SeparationCyclesManagement.parent.doesnot.have.curricularCourse.to.dismissal");
	}

	final Dismissal dismissal = new Dismissal();
	dismissal.setCredits(credits);
	dismissal.setCurriculumGroup(parent);
	dismissal.setCurricularCourse(curricularCourse);

	return dismissal;
    }

    private OptionalDismissal createNewOptionalDismissal(final Credits credits, final CurriculumGroup parent,
	    final OptionalCurricularCourse curricularCourse, final Double ectsCredits) {
	if (ectsCredits == null || ectsCredits.doubleValue() == 0) {
	    throw new DomainException("error.OptionalDismissal.invalid.credits");
	}

	if (!hasCurricularCourseToDismissal(parent, curricularCourse, getExecutionYear())) {
	    throw new DomainException("error.SeparationCyclesManagement.parent.doesnot.have.curricularCourse.to.dismissal");
	}

	final OptionalDismissal dismissal = new OptionalDismissal();
	dismissal.setCredits(credits);
	dismissal.setCurriculumGroup(parent);
	dismissal.setCurricularCourse(curricularCourse);
	dismissal.setEctsCredits(ectsCredits);

	return dismissal;
    }

    private boolean hasCurricularCourseToDismissal(final CurriculumGroup curriculumGroup,
	    final CurricularCourse curricularCourse, final ExecutionYear executionYear) {

	final CourseGroup degreeModule = curriculumGroup.getDegreeModule();
	for (final Context context : degreeModule.getValidChildContexts(CurricularCourse.class, executionYear)) {
	    final CurricularCourse each = (CurricularCourse) context.getChildDegreeModule();
	    if (each == curricularCourse && !curriculumGroup.hasChildDegreeModule(degreeModule)) {
		return true;
	    }
	}
	return false;
    }

    private void createDismissal(final Dismissal dismissal, final CurriculumGroup parent) {
	if (curriculumGroupHasSimilarDismissal(parent, dismissal)) {
	    return;
	}

	final Credits credits = dismissal.getCredits();

	final Credits newCredits;
	if (credits.isTemporary()) {
	    newCredits = new TemporarySubstitution();

	} else if (credits.isSubstitution()) {
	    newCredits = new Substitution();

	} else if (credits.isEquivalence()) {
	    final Equivalence equivalence = (Equivalence) credits;
	    final Equivalence newEquivalence = new Equivalence();
	    equivalence.setGrade(equivalence.getGrade());
	    newCredits = newEquivalence;

	} else {
	    newCredits = new Credits();
	}

	newCredits.setStudentCurricularPlan(parent.getStudentCurricularPlan());
	newCredits.setExecutionPeriod(credits.getExecutionPeriod());
	newCredits.setGivenCredits(credits.getGivenCredits());

	for (final IEnrolment enrolment : credits.getIEnrolments()) {
	    EnrolmentWrapper.create(newCredits, enrolment);
	}

	if (dismissal.hasCurricularCourse()) {
	    if (dismissal instanceof OptionalDismissal) {
		final OptionalDismissal optionalDismissal = (OptionalDismissal) dismissal;
		createNewOptionalDismissal(newCredits, parent,
			(OptionalCurricularCourse) optionalDismissal.getCurricularCourse(), optionalDismissal.getEctsCredits());

	    } else {
		createNewDismissal(newCredits, parent, dismissal.getCurricularCourse());
	    }
	} else if (dismissal.isCreditsDismissal()) {
	    final CreditsDismissal creditsDismissal = (CreditsDismissal) dismissal;
	    new CreditsDismissal(newCredits, parent, creditsDismissal.getNoEnrolCurricularCourses());
	} else {
	    throw new DomainException("error.unknown.dismissal.type");
	}
    }

    private boolean curriculumGroupHasSimilarDismissal(final CurriculumGroup curriculumGroup, final Dismissal dismissal) {
	for (final Dismissal each : curriculumGroup.getChildDismissals()) {
	    if (each.isSimilar(dismissal)) {
		return true;
	    }
	}
	return false;
    }

    private void moveExtraCurriculumGroupInformation(final StudentCurricularPlan oldStudentCurricularPlan,
	    final StudentCurricularPlan newStudentCurricularPlan) {

	final ExtraCurriculumGroup oldExtraCurriculumGroup = oldStudentCurricularPlan.getExtraCurriculumGroup();
	if (oldExtraCurriculumGroup != null) {
	    final ExtraCurriculumGroup newExtraCurriculumGroup = newStudentCurricularPlan.getExtraCurriculumGroup();
	    if (newExtraCurriculumGroup == null) {
		throw new DomainException("error.invalid.newExtraCurriculumGroup");
	    }
	    for (final CurriculumModule curriculumModule : oldExtraCurriculumGroup.getCurriculumModulesSet()) {
		curriculumModule.setCurriculumGroup(newExtraCurriculumGroup);
	    }

	    for (final CurriculumLine curriculumLine : newExtraCurriculumGroup.getAllCurriculumLines()) {
		if (curriculumLine.isDismissal()) {
		    final Dismissal dismissal = (Dismissal) curriculumLine;
		    dismissal.getCredits().setStudentCurricularPlan(newStudentCurricularPlan);
		}
	    }
	}
    }

    private void tryRemoveOldSecondCycle(final CycleCurriculumGroup oldSecondCycle) {
	if (canRemoveOldSecondCycle(oldSecondCycle)) {
	    deleteCurriculumModules(oldSecondCycle);
	}
    }

    protected void deleteCurriculumModules(final CurriculumModule curriculumModule) {
	if (curriculumModule == null) {
	    return;
	}
	if (!curriculumModule.isLeaf()) {
	    final CurriculumGroup curriculumGroup = (CurriculumGroup) curriculumModule;
	    for (; curriculumGroup.hasAnyCurriculumModules();) {
		deleteCurriculumModules(curriculumGroup.getCurriculumModules().get(0));
	    }
	    curriculumGroup.delete();
	} else if (curriculumModule.isDismissal()) {
	    curriculumModule.delete();
	} else {
	    throw new DomainException("error.can.only.remove.groups.and.dismissals");
	}
    }

    private boolean canRemoveOldSecondCycle(final CycleCurriculumGroup oldSecondCycle) {
	for (final CurriculumLine curriculumLine : oldSecondCycle.getAllCurriculumLines()) {
	    if (curriculumLine.isEnrolment()) {
		return false;
	    } else if (!curriculumLine.isDismissal()) {
		throw new DomainException("error.unknown.curriculum.line");
	    }
	}
	return true;
    }

    private void markOldRegistrationWithConcludedState(final StudentCurricularPlan oldStudentCurricularPlan) {
	if (oldStudentCurricularPlan.getRegistration().hasState(RegistrationStateType.CONCLUDED)) {
	    return;
	}
	final RegistrationState state = RegistrationState.createState(oldStudentCurricularPlan.getRegistration(), null,
		new DateTime(), RegistrationStateType.CONCLUDED);
	state.setResponsiblePerson(null);
    }

    private void moveGratuityEventsInformation(final StudentCurricularPlan oldStudentCurricularPlan,
	    final StudentCurricularPlan newStudentCurricularPlan) {

	if (!oldStudentCurricularPlan.hasGratuityEvent(getExecutionYear())
		|| oldStudentCurricularPlan.getGratuityEvent(getExecutionYear()).isCancelled()) {
	    return;
	}

	if (!newStudentCurricularPlan.hasGratuityEvent(getExecutionYear())) {
	    createGratuityEvent(newStudentCurricularPlan);
	}

	final GratuityEvent firstEvent = oldStudentCurricularPlan.getGratuityEvent(getExecutionYear());
	final GratuityEvent secondEvent = newStudentCurricularPlan.getGratuityEvent(getExecutionYear());

	if (!firstEvent.isGratuityEventWithPaymentPlan() || !secondEvent.isGratuityEventWithPaymentPlan()) {
	    throw new DomainException("error.SeparationCyclesManagement.unexpected.event.types");
	}

	movePayments((GratuityEventWithPaymentPlan) firstEvent, (GratuityEventWithPaymentPlan) secondEvent);
    }

    private void createGratuityEvent(final StudentCurricularPlan newStudentCurricularPlan) {
	final InvocationResult result = new AccountingEventsManager().createGratuityEvent(newStudentCurricularPlan,
		getExecutionYear(), false);
	if (!result.isSuccess()) {
	    throw new DomainExceptionWithInvocationResult(result);
	}
    }

    private void movePayments(final GratuityEventWithPaymentPlan firstEvent, final GratuityEventWithPaymentPlan secondEvent) {
	if (firstEvent.isClosed()) {
	    if (canAddExemption(secondEvent)) {
		createPercentageGratuityExemptionForSecondDebt(firstEvent, secondEvent);
	    }
	} else if (firstEvent.hasAnyPayments()) {
	    if (secondEvent.hasCustomGratuityPaymentPlan()) {
		throw new DomainException("error.SeparationCyclesManagement.secondEvent.already.has.custom.payment.plan");
	    }
	    createNewPaymentPlan(firstEvent, secondEvent);
	} else {
	    firstEvent.cancel(getNoPaymentsReason(secondEvent));
	}
    }

    private void createNewPaymentPlan(final GratuityEventWithPaymentPlan firstEvent,
	    final GratuityEventWithPaymentPlan secondEvent) {

	createValueGratuityExemption(firstEvent, secondEvent, firstEvent.calculateAmountToPay(new DateTime()));

	Money amountLessPenalty = firstEvent.getPayedAmountLessPenalty();
	final PaymentPlan oldPaymentPlan = secondEvent.getGratuityPaymentPlan();

	final Set<Installment> installmentsToCreate = new HashSet<Installment>();

	for (final Installment installment : oldPaymentPlan.getInstallmentsSortedByEndDate()) {
	    if (amountLessPenalty.greaterOrEqualThan(installment.getAmount())) {
		amountLessPenalty = amountLessPenalty.subtract(installment.getAmount());
	    } else {
		installmentsToCreate.add(installment);
	    }
	}

	secondEvent.configureCustomPaymentPlan();

	final PaymentPlan newPaymentPlan = secondEvent.getGratuityPaymentPlan();
	for (final Installment installment : installmentsToCreate) {
	    createInstallment(installment, newPaymentPlan, installment.getAmount().subtract(amountLessPenalty));
	    amountLessPenalty = Money.ZERO;
	}

	secondEvent.recalculateState(new DateTime());
    }

    private void createValueGratuityExemption(final GratuityEventWithPaymentPlan firstEvent,
	    final GratuityEventWithPaymentPlan secondEvent, final Money amountToPay) {
	new ValueGratuityExemption(firstEvent, GratuityExemptionJustificationType.SEPARATION_CYCLES_AUTHORIZATION,
		getReasonWhenHasAnyPaymentsInFirstDebt(secondEvent), new YearMonthDay(), amountToPay);
    }

    private void createPercentageGratuityExemptionForSecondDebt(final GratuityEventWithPaymentPlan firstEvent,
	    final GratuityEventWithPaymentPlan secondEvent) {
	new PercentageGratuityExemption(secondEvent, GratuityExemptionJustificationType.SEPARATION_CYCLES_AUTHORIZATION,
		getIsClosedReason(firstEvent), new YearMonthDay(), BigDecimal.valueOf(1));
    }

    private void createInstallment(final Installment installment, final PaymentPlan paymentPlan, final Money amount) {
	if (installment.isWithMonthlyPenalty()) {
	    final InstallmentWithMonthlyPenalty penalty = (InstallmentWithMonthlyPenalty) installment;
	    new InstallmentWithMonthlyPenalty(paymentPlan, amount, penalty.getStartDate(), penalty.getEndDate(), penalty
		    .getPenaltyPercentage(), penalty.getWhenStartToApplyPenalty(), penalty.getMaxMonthsToApplyPenalty());
	} else {
	    throw new DomainException("error.SeparationCyclesManagement.unexpected.installment.type");
	}
    }

    private boolean canAddExemption(final GratuityEventWithPaymentPlan secondEvent) {
	return !secondEvent.hasGratuityExemption() && !secondEvent.isClosed() && !secondEvent.hasAnyPayments()
		&& !secondEvent.hasCustomGratuityPaymentPlan();
    }

    private String getReasonWhenHasAnyPaymentsInFirstDebt(final GratuityEvent second) {
	final String message = RESOURCE_BUNDLE.getString("label.SeparationCyclesManagement.hasAnyPaymentsInFirstDebt.reason");
	return MessageFormat.format(message, second.getStudentCurricularPlan().getName());
    }

    private String getIsClosedReason(final GratuityEvent event) {
	final String message = RESOURCE_BUNDLE.getString("label.SeparationCyclesManagement.isClosed.reason");
	return MessageFormat.format(message, event.getStudentCurricularPlan().getName(), event.getExecutionYear().getName());
    }

    private String getNoPaymentsReason(final GratuityEvent second) {
	final String message = RESOURCE_BUNDLE.getString("label.SeparationCyclesManagement.noPayments.reason");
	return MessageFormat.format(message, second.getStudentCurricularPlan().getName());
    }

    private void createAdministrativeOfficeFeeAndInsurance(final StudentCurricularPlan newStudentCurricularPlan) {
	if (!newStudentCurricularPlan.getPerson().hasAdministrativeOfficeFeeInsuranceEventFor(getExecutionYear())) {
	    new AccountingEventsManager().createAdministrativeOfficeFeeAndInsuranceEvent(newStudentCurricularPlan,
		    getExecutionYear(), false);
	}
    }

    protected ExecutionSemester getExecutionPeriod() {
	return ExecutionSemester.readActualExecutionSemester();
    }

    private ExecutionYear getExecutionYear() {
	return getExecutionPeriod().getExecutionYear();
    }

}
