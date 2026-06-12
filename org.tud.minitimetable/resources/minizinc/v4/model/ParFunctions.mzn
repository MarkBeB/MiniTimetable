include "Parameters.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTANT FUNCTIONS
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function int: toIndex(int: d, ShiftTypes: s) =
  enum2int(s) - 1 + d * length(ShiftTypes);

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
  
function bool: isOccupantAgeGroup(OccupantIds: o, AgeGroups: g) =
  g == occupantAgeGroup(o);

function Genders: occupantGender(OccupantIds: o) =
  occupants[o].gender;

function int: occupantLengthOfStay(OccupantIds: o) =
  occupants[o].lengthOfStay;
  
function int: occupantWorkload(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupants[o].workloadProduced[toIndex(d,s)];

function int: occupantSkillRequired(OccupantIds: o, Days: d, ShiftTypes: s) =
  occupants[o].skillLevelRequired[toIndex(d,s)];


% PATIENTS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function bool: isPatientOfSurgeon(PatientIds: p, SurgeonIds: s) =
  patientSurgeon(p) == s;
  
function bool: isPatientSurgeonAvailable(PatientIds: p, Days: d) =
  isSurgeonAvailable(patientSurgeon(p),d);

function SurgeonIds: patientSurgeon(PatientIds: p) =
  patients[p].assignedSurgeon;

function int: surgeryDuration(PatientIds: p) =
  patients[p].surgeryDuration;

function AgeGroups: patientAgeGroup(PatientIds: p) =
  patients[p].ageGroup;
  
function bool: isPatientAgeGroup(PatientIds: p, AgeGroups: g) =
  g == patientAgeGroup(p);

function Genders: patientGender(PatientIds: p) =
  patients[p].gender;

function int: patientWorkload(PatientIds: p, int: currentDay, ShiftTypes: s, int: admissionDay) =
  let {
    int: relDay = currentDay - admissionDay;
    int: idx = toIndex(relDay, s);
  } in 
  if 0 <= relDay /\ relDay < patientLengthOfStay(p) then
   patients[p].workloadProduced[idx]
  else 
    0 
  endif;

function int: patientSkillRequired(PatientIds: p, int: currentDay, ShiftTypes: s, int: admissionDay) =
  let {
    int: relDay = currentDay - admissionDay;
    int: idx = toIndex(relDay, s);
  } in 
  if 0 <= relDay /\ relDay < patientLengthOfStay(p) then
   patients[p].skillLevelRequired[idx]
  else 
    0 
  endif;

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
  
function bool: withinStayWindow(PatientIds: p, Days: d) =
  if isPatientMandatory(p) then
    earliestAdmissionDay(p) <= d /\ d < latestAdmissionDay(p) + patientLengthOfStay(p)
  else
    earliestAdmissionDay(p) <= d
  endif;
  
function bool: isPatientOpAvailable(PatientIds: p, Days: d) = 
  isPatientSurgeonAvailable(p,d) /\ isAnyOTAvailable(d);

function bool: canPatientBePresent(PatientIds: p, Days: d) =
  withinStayWindow(p,d) /\ (card(compatibleRooms(p)) > 0);

function bool: canPatientBePresent(PatientIds: p, Days: d, RoomIds: r) =
  withinStayWindow(p,d) /\ (r in compatibleRooms(p));
  
function set of ExtendedDays: validAdmissionDays(PatientIds: p) =
  if isPatientMandatory(p) then
    { d | d in Days where (earliestAdmissionDay(p) <= d) /\ (d <= latestAdmissionDay(p)) /\ isPatientOpAvailable(p,d) }
  else
    { extendedDay } union % postponed day
    { d | d in Days where (earliestAdmissionDay(p) <= d) /\ isPatientOpAvailable(p,d) }
  endif;

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

function set of NurseIds: availableNurses(Days: d, ShiftTypes: s) =
  { n | n in NurseIds where isNurseAvailable(n,d,s) };


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
  
function int: roomOccupants(RoomIds: r, Days: d) =
  sum(o in OccupantIds)(isOccupantInRoom(o,d,r));
  
function bool: roomHasOccupants(RoomIds: r, Days: d) =
  roomOccupants(r,d) > 0;
 