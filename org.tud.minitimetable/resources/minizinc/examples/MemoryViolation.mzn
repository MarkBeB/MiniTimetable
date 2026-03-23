enum PatientIds = {p01, p02, p03, p04};

type Patient = record (
  par PatientIds: id,
);

type PatientVar = record (
  var int: Attribute1,
  var int: Attribute2,
  var int: Attribute3,
  var int: Attribute4,
  var int: Attribute5,
  var int: Attribute6,
  var int: Attribute7,
  var int: Attribute8,
  var int: Attribute9,
  array[1..2] of var int: Attribute10,
);
type ExtendedPatient = Patient ++ PatientVar;

array[PatientIds] of Patient: patients = [
  (id: p01),
  (id: p02),
  (id: p03),
  (id: p04),
];

array[PatientIds] of PatientVar: patientVar;

% MiniZinc error: Memory violation detected (segmentation fault).
% Process finished with non-zero exit code -1073740791.
array[PatientIds] of ExtendedPatient: extendedPatients = [id: patients[id] ++ patientVar[id] | id in PatientIds];


