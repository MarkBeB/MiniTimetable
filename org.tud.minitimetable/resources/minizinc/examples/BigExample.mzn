int: lastDay = 7;
set of int: Days = 1..lastDay;
type MinutesPerDay = 0..(24 * 60);

enum SurgeonIds;
enum PatientIds;

type Surgeon = record(
  SurgeonIds: id,
  array[Days] of MinutesPerDay: maxSurgeryTime,
);

type Patient = record (
  PatientIds: id,
  SurgeonIds: assignedSurgeon,
  int: surgeryDuration,
);

type PatientVar = record (
  var int: Attribute1,
  var int: Attribute2,
  var int: Attribute3,
  var int: Attribute4,
  var int: Attribute5,
  var int: Attribute6,
  var int: Attribute7,
  array[SurgeonIds] of var int: Attribute8,
);
type ExtendedPatient = Patient ++ PatientVar;

array[PatientIds] of Patient: patients;
array[SurgeonIds] of Surgeon: surgeons;

array[PatientIds] of var Days: admissionDay;
array[SurgeonIds, Days] of var MinutesPerDay: surgeryTime;

array[PatientIds] of PatientVar: patientVar;
array[PatientIds] of ExtendedPatient: extendedPatients ::output = [id: patients[id] ++ patientVar[id] | id in PatientIds];

constraint forall(surgeon in surgeons, day in Days)(
  surgeryTime[surgeon.id, day] = sum(patient in patients where patient.assignedSurgeon == surgeon.id /\ admissionDay[patient.id] == day)(patient.surgeryDuration)
);

constraint forall(surgeon in surgeons, day in Days)(
  surgeon.maxSurgeryTime[day] >= surgeryTime[surgeon.id, day]
);

%solve satisfy;
solve minimize count(surgeon in surgeons, day in Days)(surgeryTime[surgeon.id, day] > 0);


