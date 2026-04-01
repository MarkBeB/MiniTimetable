include "../v1/IHTP-model.mzn";
%include "Variables.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANT FUNCTIONS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function int: toIndex(int: d, ShiftTypes: s) =
   enum2int(s) - 1 + d * length(ShiftTypes);

% these functions are used to access parameters. It looks cleaner.

function bool: isOccupantInRoom(OccupantIds: o, RoomIds: r) = 
  occupantData[o].assignedRoom == r;

function bool: isOccupantPresentInRoom(OccupantIds: o, Days: d, RoomIds: r) = 
  isOccupantInRoom(o,r) /\ isOccupantPresent(o,d);

function AgeGroups: occupantAgeGroup(OccupantIds: o) =
  occupantData[o].ageGroup;

function Genders: occupantGender(OccupantIds: o) =
  occupantData[o].gender;

function bool: isOccupantPresent(OccupantIds: id, Days: day) = 
  day < occupantData[id].lengthOfStay;

function RoomIds: occupantRoom(OccupantIds: o) =
  occupantData[o].assignedRoom;

function int: occupantWorkload(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupantData[o].workloadProduced[occupantDailyIndex(d,s)];

function int: occupantSkillLevel(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupantData[o].skillLevelRequired[occupantDailyIndex(d,s)];

function int: occupantLengthOfStay(OccupantIds: o) =
  occupantData[o].lengthOfStay;

function int: occupantDailyIndex(Days: d, ShiftTypes: s) =
  enum2int(s) - 1 + enum2int(d) * length(ShiftTypes);



function AgeGroups: patientAgeGroup(PatientIds: p) =
  patientData[p].ageGroup;

function Genders: patientGender(PatientIds: p) =
  patientData[p].gender;

function bool: patientOfSurgeon(PatientIds: p, SurgeonIds: s) =
  assignedSurgeon(p) == s;

function SurgeonIds: assignedSurgeon(PatientIds: p) =
  patientData[p].assignedSurgeon;

function int: surgeryDuration(PatientIds: p) =
  patientData[p].surgeryDuration;

function int: patientWorkload(PatientIds: p, int: relativeDay, ShiftTypes: s) =
  patientData[p].workloadProduced[toIndex(relativeDay,s)];

function int: patientSkillLevel(PatientIds: p, int: relativeDay, ShiftTypes: s) =
  patientData[p].skillLevelRequired[toIndex(relativeDay,s)];

function int: patientLengthOfStay(PatientIds: p) =
  patientData[p].lengthOfStay;

function set of RoomIds: incompatibleRooms(PatientIds: p) =
  array2set(patientData[p].incompatibleRooms);

function set of RoomIds: compatibleRooms(PatientIds: p) =
  RoomIds diff incompatibleRooms(p);

function bool: isPatientMandatory(PatientIds: p) = 
  patientData[p].mandatory;

function bool: isPatientOptional(PatientIds: p) = 
  not patientData[p].mandatory;
  
function Days: earliestAdmissionDay(PatientIds: p) =
  patientData[p].earliestPossibleAdmission;

function Days: latestAdmissionDay(PatientIds: p) =
  patientData[p].latestPossibleAdmission;
  
  


function int: nurseWorkloadCapacity(NurseIds: n, Days: d, ShiftTypes: s) =
  nurseData[n].maxWorkloadPerShift[d, s];

function int: nurseSkillLevel(NurseIds: n) =
  nurseData[n].skillLevel;

function bool: isNurseAvailable(NurseIds: n, Days: d, ShiftTypes: s) =
  nurseWorkloadCapacity(n,d,s) > 0;

function bool: isNurseAvailable(NurseIds: n, Days: d) =
  exists(s in ShiftTypes)(isNurseAvailable(n,d,s));




function bool: isSurgeonAvailable(SurgeonIds: s, Days: d) = 
  surgeonSurgeryCapacity(s,d) > 0;

function int: surgeonSurgeryCapacity(SurgeonIds: s, Days: d) = 
  surgeonData[s].maxSurgeryTime[d];

  



function bool: isOTAvailable(OperatingTheaterIds: ot, Days: d) = 
  otSurgeryCapacity(ot,d) > 0;

function int: otSurgeryCapacity(OperatingTheaterIds: ot, Days: d) = 
  operatingTheaterData[ot].availability[d];





function int: roomCapacity(RoomIds: r) =
  roomData[r].capacity;
 