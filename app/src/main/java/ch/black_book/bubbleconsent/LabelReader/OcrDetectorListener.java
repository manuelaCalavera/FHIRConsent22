package ch.black_book.bubbleconsent.LabelReader;

public interface OcrDetectorListener {
    // These methods are the different events and
    // need to pass relevant arguments related to the event triggered
    public void onPatientDetected(PatientRecord patientRecord);
}