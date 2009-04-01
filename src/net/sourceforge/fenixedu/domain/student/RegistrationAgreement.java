package net.sourceforge.fenixedu.domain.student;

public enum RegistrationAgreement {

    NORMAL(true, true),

    AFA(false, false),

    MA(false, false),

    NC(false, false),

    ERASMUS(false, false),

    SOCRATES(false, false),

    SOCRATES_ERASMUS(false, false),

    TEMPUS(false, false),

    BILATERAL_AGREEMENT(false, false),

    ALFA2(false, false),

    UNIFOR(false, false),

    TIME(false, false),

    TOTAL(false, true),

    OTHER_EXTERNAL(false, false),

    MITP(false, true),

    SMILE(false, false),

    ANGOLA_TELECOM(false, true);

    private boolean enrolmentByStudentAllowed;

    private boolean payGratuity;

    private RegistrationAgreement(final boolean enrolmentByStudentAllowed, final boolean payGratuity) {
	this.enrolmentByStudentAllowed = enrolmentByStudentAllowed;
	this.payGratuity = payGratuity;
    }

    public boolean isNormal() {
	return this.equals(RegistrationAgreement.NORMAL);
    }

    public boolean isMilitaryAgreement() {
	return this.equals(RegistrationAgreement.AFA) || this.equals(RegistrationAgreement.MA);
    }

    public String getName() {
	return name();
    }

    public boolean isEnrolmentByStudentAllowed() {
	return this.enrolmentByStudentAllowed;
    }

    public boolean isToPayGratuity() {
	return payGratuity;
    }

    public static RegistrationAgreement getByLegacyCode(int code) {

	switch (code) {
	case 1:
	    return AFA;
	case 2:
	    return MA;
	case 3:
	    return NC;
	case 4:
	    return ERASMUS;
	case 5:
	    return SOCRATES;
	case 6:
	    return SOCRATES_ERASMUS;
	case 7:
	    return TEMPUS;
	case 8:
	    return BILATERAL_AGREEMENT;
	case 9:
	    return ALFA2;
	case 10:
	    return UNIFOR;
	}

	return null;
    }

    public String getQualifiedName() {
	return getClass().getSimpleName() + "." + name();
    }

}
