%include "Parameters.mzn";
%include "Functions.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% DECISION VARIABLES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% PATIENTS

array[PatientIds, RoomIds] of var bool: isPatientInRoom;
array[PatientIds] of var opt RoomIds: patientRoomAssignment; % set to room r if patient p is admitted
% link arrays
constraint forall(p in PatientIds, r in RoomIds)(
  isPatientInRoom[p,r] == (patientRoomAssignment[p] == r)
);


array[PatientIds, ExtendedDays] of var bool: isPatientAdmittedAt;
array[PatientIds] of var ExtendedDays: patientAdmissionDay;
array[PatientIds, Days] of var bool: isPatientPresent;
% link arrays
constraint forall(p in PatientIds, d in ExtendedDays)(
  isPatientAdmittedAt[p,d] == (patientAdmissionDay[p] == d)
);

array[PatientIds, Days, RoomIds] of var bool: isPatientPresentInRoom = [
  (p,d,r): isPatientInRoom[p,r] /\ isPatientPresent[p,d] | p in PatientIds, r in RoomIds, d in Days
];

constraint forall(p in PatientIds, d in Days)(
  isPatientPresent[p,d] == (patientAdmissionDay[p] <= d /\ d <= patientAdmissionDay[p] + patientLengthOfStay(p))
);

array[PatientIds] of var bool: isPatientAdmitted;
array[PatientIds] of var bool: isPatientPostponed = [ not isPatientAdmitted[p] | p in PatientIds ];
constraint forall(p in PatientIds)(
  sum(d in Days)(isPatientAdmittedAt[p,d]) == isPatientAdmitted[p] 
  /\ sum(d in ExtendedDays)(isPatientAdmittedAt[p,d]) == 1
);

array[PatientIds, OperatingTheaterIds] of var bool: isPatientInOT;
array[PatientIds] of var opt OperatingTheaterIds: patientOTAssignment;
% link arrays
constraint forall(p in PatientIds, ot in OperatingTheaterIds)(
  isPatientInOT[p,ot] == (patientOTAssignment[p] == ot)
);


% ROOMS
array[RoomIds, Days] of var bool: isRoomOccupied;
constraint forall(r in RoomIds, d in Days)(
  let {
    bool: hasOccupants = exists(o in OccupantIds)(isOccupantInRoom(o,d,r));
  } in
  if hasOccupants then
    isRoomOccupied[r,d] = true % fixed, a room with an occupant is always occupied
  else
    isRoomOccupied[r,d] = sum(p in PatientIds)(isPatientInRoom[p,r] * isPatientPresent[p,d])
  endif
);

array[RoomIds, Days, ShiftTypes] of var opt NurseIds: roomNurseAssignment;
array[NurseIds, Days, RoomIds] of var bool: nurseWorksInRoom = [(n,d,r):
  sum(s in ShiftTypes)(roomNurseAssignment[r,d,s]==n)>0 | n in NurseIds, r in RoomIds, d in Days
];

% NURSES
%array[NurseIds, Days, RoomIds] of var bool: isNurseInRoom = [(n,d,r): sum(s in ShiftTypes)(roomNurseAssignment[r,d,s] == n) > 0 | n in NurseIds, d in Days, r in RoomIds];
%array[NurseIds, Days, RoomIds] of var bool: isNurseInRoom2 = [(n,d,r): sum(s in ShiftTypes)(isNurseInRoomAt[n,d,r,s]) > 0 | n in NurseIds, d in Days, r in RoomIds];
%array[NurseIds, Days, RoomIds, ShiftTypes] of var bool: isNurseInRoomAt;

%constraint forall(n in NurseIds, d in Days, s in ShiftTypes)(
%  not isNurseAvailable(n,d,s) -> forall(r in RoomIds)(isNurseInRoomAt[n,d,r,s] = false)
%);

array[PatientIds, NurseIds] of var bool: patientCaredByNurse;
constraint forall(p in PatientIds, n in NurseIds)(
  % patientCaredByNurse[p,n] <-> 0 < sum(r in RoomIds, d in Days)(isPatientPresent[p,d] * isPatientInRoom[p,r] * nurseWorksInRoom[n,r,d])
  patientCaredByNurse[p,n] <-> 0 < sum(r in RoomIds, d in Days)(isPatientPresentInRoom[p,d,r] * nurseWorksInRoom[n,r,d])
);

array[OccupantIds, NurseIds] of var bool: occupantCaredByNurse;
constraint forall(o in OccupantIds, n in NurseIds)(
  occupantCaredByNurse[o,n] <-> 0 < sum(r in RoomIds, d in Days)(isOccupantInRoom(o,d,r) * nurseWorksInRoom[n,r,d])
);

% OVERVIEW

%var int: mandatoryPatients :: output = sum(p in PatientIds)(isPatientMandatory(p));
%var int: admittedPatients ::output = sum(p in PatientIds)(isPatientAdmitted[p]);
%var int: postponedPatients ::output = sum(p in PatientIds)(isPatientPostponed[p]) ;
%var int: totalPatients ::output = card(PatientIds);

%array[RoomIds, Days] of var 0..1000: roomUtilization;
%array[RoomIds, Days] of var 0..1000: roomWorkload;