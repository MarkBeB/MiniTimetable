include "Parameters.mzn";
%include "Variables.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANT FUNCTIONS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


% OCCUPANTS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function bool: isOccupantInRoom(OccupantIds: o, RoomIds: r) = 
  occupants[o].assignedRoom == r;

function bool: isOccupantInRoom(OccupantIds: o, Days: d, RoomIds: r) = 
  isOccupantInRoom(o,r) /\ isOccupantPresent(o,d);

function bool: isOccupantPresent(OccupantIds: id, Days: day) = 
  day < occupants[id].lengthOfStay;

function RoomIds: occupantRoom(OccupantIds: o) =
  occupants[o].assignedRoom;

function AgeGroups: occupantAgeGroup(OccupantIds: o) =
  occupants[o].ageGroup;

function Genders: occupantGender(OccupantIds: o) =
  occupants[o].gender;

function int: occupantLengthOfStay(OccupantIds: o) =
  occupants[o].lengthOfStay;
  
function int: occupantWorkload(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupants[o].workloadProduced[occupantIndex(d,s)];

function int: occupantSkillRequired(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupants[o].skillLevelRequired[occupantIndex(d,s)];

function int: occupantIndex(Days: d, ShiftTypes: s) =
  enum2int(s) - 1 + enum2int(d) * length(ShiftTypes);

function int: toIndex(Days: d, ShiftTypes: s) =
  enum2int(s) - 1 + enum2int(d) * length(ShiftTypes);



% PATIENTS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function bool: isPatientOfSurgeon(PatientIds: p, SurgeonIds: s) =
  assignedSurgeon(p) == s;
  
function bool: isPatientSurgeonAvailable(PatientIds: p, Days: d) =
  isSurgeonAvailable(assignedSurgeon(p),d);

function SurgeonIds: assignedSurgeon(PatientIds: p) =
  patients[p].assignedSurgeon;

function int: surgeryDuration(PatientIds: p) =
  patients[p].surgeryDuration;

function AgeGroups: patientAgeGroup(PatientIds: p) =
  patients[p].ageGroup;

function Genders: patientGender(PatientIds: p) =
  patients[p].gender;

function var int: patientWorkload(PatientIds: p, var int: relativeDay, ShiftTypes: s) =
  patients[p].workloadProduced[toIndex(relativeDay,s)];

function var int: patientSkillRequired(PatientIds: p, var int: relativeDay, ShiftTypes: s) =
  patients[p].skillLevelRequired[toIndex(relativeDay,s)];

function var int: toIndex(var int: d, ShiftTypes: s) =
  enum2int(s) - 1 + d * length(ShiftTypes);

function int: patientLengthOfStay(PatientIds: p) =
  patients[p].lengthOfStay;

function set of RoomIds: incompatibleRooms(PatientIds: p) =
  array2set(patients[p].incompatibleRooms);

function set of RoomIds: compatibleRooms(PatientIds: p) =
  RoomIds diff incompatibleRooms(p);

function bool: isPatientMandatory(PatientIds: p) = 
  patients[p].mandatory;

function bool: isPatientOptional(PatientIds: p) = 
  not patients[p].mandatory;
  
function Days: earliestAdmissionDay(PatientIds: p) =
  patients[p].earliestPossibleAdmission;

function Days: latestAdmissionDay(PatientIds: p) =
  patients[p].latestPossibleAdmission;

function bool: isPatientAdmittable(PatientIds: p, Days: d) = true;
%  (earliestAdmissionDay(p) <= d) /\ (card(compatibleRooms(p)) > 0) /\ isAnyOTAvailable(d) /\ isPatientSurgeonAvailable(p, d);


% NURSES
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function int: nurseWorkloadCapacity(NurseIds: n, Days: d, ShiftTypes: s) =
  nurses[n].maxWorkloadPerShift[d, s];

function int: nurseSkillLevel(NurseIds: n) =
  nurses[n].skillLevel;

function bool: isNurseAvailable(NurseIds: n, Days: d, ShiftTypes: s) =
  nurseWorkloadCapacity(n,d,s) > 0;

function bool: isNurseAvailable(NurseIds: n, Days: d) =
  exists(s in ShiftTypes)(isNurseAvailable(n,d,s));



% SURGEONS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function bool: isSurgeonAvailable(SurgeonIds: s, Days: d) = 
  surgeonSurgeryCapacity(s,d) > 0;

function int: surgeonSurgeryCapacity(SurgeonIds: s, Days: d) = 
  surgeons[s].maxSurgeryTime[d];

  

% OT
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function bool: isOTAvailable(OperatingTheaterIds: ot, Days: d) = 
  otSurgeryCapacity(ot,d) > 0;

function bool: isAnyOTAvailable(Days: d) =
  exists(ot in OperatingTheaterIds)(isOTAvailable(ot,d));

function int: otSurgeryCapacity(OperatingTheaterIds: ot, Days: d) = 
  operatingTheaters[ot].availability[d];



% ROOMS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function int: roomCapacity(RoomIds: r) =
  rooms[r].capacity;
 