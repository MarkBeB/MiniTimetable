include "Parameters.mzn";
include "Functions.mzn";

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% DECISION VARIABLES
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% PATIENTS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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
  isPatientPresent[p,d] <-> (patientAdmissionDay[p] <= d /\ d <= patientAdmissionDay[p] + patientLengthOfStay(p))
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
constraint forall(p in PatientIds)(
  sum(ot in OperatingTheaterIds)(isPatientInOT[p,ot]) <= 1
);


% ROOMS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

array[RoomIds, Days] of var bool: isRoomOccupied;
constraint forall(r in RoomIds, d in Days)(
  let {
    bool: hasOccupants = exists(o in OccupantIds)(isOccupantPresentInRoom(o,d,r));
  } in
  if hasOccupants then
    isRoomOccupied[r,d] = true % fixed, a room with an occupant is always occupied
  else
    isRoomOccupied[r,d] <-> exists(p in PatientIds)(isPatientPresentInRoom[p,d,r])
  endif
);

array[RoomIds, Days, ShiftTypes] of var 0..100000: roomWorkload;

array[RoomIds, Days, ShiftTypes] of var opt NurseIds: roomNurseAssignment;
constraint forall(n in NurseIds, d in Days, r in RoomIds)(
  isNurseInRoom[n,d,r] = sum(s in ShiftTypes)(roomNurseAssignment[r,d,s]==n) 
); 

% NURSES
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%array[NurseIds, Days, RoomIds] of var bool: isNurseInRoom = [(n,d,r): sum(s in ShiftTypes)(roomNurseAssignment[r,d,s] == n) > 0 | n in NurseIds, d in Days, r in RoomIds];
%array[NurseIds, Days, RoomIds] of var bool: isNurseInRoom2 = [(n,d,r): sum(s in ShiftTypes)(isNurseInRoomAt[n,d,r,s]) > 0 | n in NurseIds, d in Days, r in RoomIds];
%array[NurseIds, Days, RoomIds, ShiftTypes] of var bool: isNurseInRoomAt;

%constraint forall(n in NurseIds, d in Days, s in ShiftTypes)(
%  not isNurseAvailable(n,d,s) -> forall(r in RoomIds)(isNurseInRoomAt[n,d,r,s] = false)
%);

array[NurseIds, Days, ShiftTypes, RoomIds] of var bool: isNursePresentInRoom;
constraint forall(n in NurseIds, d in Days, s in ShiftTypes where not isNurseAvailable(n,d,s))(
  forall(r in RoomIds)(isNursePresentInRoom[n,d,s,r] = false)
);

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  sum(n in NurseIds)(isNursePresentInRoom[n,d,s,r]) <= 1
);

array[NurseIds, Days, RoomIds] of var bool: isNurseInRoom;
constraint forall(n in NurseIds, d in Days, r in RoomIds)(
  isNurseInRoom[n,d,r] = sum(s in ShiftTypes)(isNursePresentInRoom[n,d,s,r]) 
);

array[PatientIds, NurseIds] of var bool: patientCaredByNurse;
constraint forall(p in PatientIds, n in NurseIds)(
  patientCaredByNurse[p,n] <-> (0 < sum(r in RoomIds, d in Days)(isPatientPresentInRoom[p,d,r] * isNurseInRoom[n,d,r]))
);

array[OccupantIds, NurseIds] of var bool: occupantCaredByNurse;
constraint forall(o in OccupantIds, n in NurseIds)(
  occupantCaredByNurse[o,n] <-> (0 < sum(r in RoomIds, d in Days)(isOccupantPresentInRoom(o,d,r) * isNurseInRoom[n,d,r]))
);


% OPERATING THEATERS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

array[OperatingTheaterIds, Days] of var DayTime: otSurgeryTime;
constraint forall(ot in OperatingTheaterIds, d in Days)(
 otSurgeryTime[ot,d] = sum(p in PatientIds where isPatientAdmittedAt[p,d] /\ isPatientInOT[p,ot])(
      surgeryDuration(p)
    )
);

% SURGEONS
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

array[SurgeonIds, Days] of var DayTime: surgeonSurgeryTime;
constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s,d] = sum(p in PatientIds where patientOfSurgeon(p,s) /\ (patientAdmissionDay[p] == d))(
    surgeryDuration(p)
  )
);

/*
constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s,d] = sum(p in PatientIds where patientOfSurgeon(p,s))(
    surgeryDuration(p) * (patientAdmissionDay[p] == d)
  )
);
*/

/*
constraint forall(s in SurgeonIds, d in Days)(
  surgeonSurgeryTime[s,d] = sum(p in PatientIds where patientOfSurgeon(p,s))(
    surgeryDuration(p) * isPatientAdmittedAt[p,d]
  )
);
*/

% OVERVIEW

%var int: mandatoryPatients :: output = sum(p in PatientIds)(isPatientMandatory(p));
%var int: admittedPatients ::output = sum(p in PatientIds)(isPatientAdmitted[p]);
%var int: postponedPatients ::output = sum(p in PatientIds)(isPatientPostponed[p]) ;
%var int: totalPatients ::output = card(PatientIds);

%array[RoomIds, Days] of var 0..1000: roomUtilization;
%array[RoomIds, Days] of var 0..1000: roomWorkload;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% CONSTRAINTS TO IMPROVE PERFORMANCE BY REDUCING SEARCH SPACE
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Prohibit admission on days when there is no OT capacity.
set of Days: daysWithoutOTCapacity = { d | d in Days where not exists(ot in OperatingTheaterIds)(isOTAvailable(ot, d))};
constraint redundant_constraint(forall(p in PatientIds, d in daysWithoutOTCapacity)(
  isPatientAdmittedAt[p,d] = false
));

% Prohibit admission on days when there is no surgeon available.
constraint redundant_constraint(forall(p in PatientIds)(
  let {
    SurgeonIds: s = assignedSurgeon(p);
    set of Days: daysWithoutSurgeonCapacity = { d | d in Days where not isSurgeonAvailable(s,d)};
  } in
  forall(d in daysWithoutSurgeonCapacity)(
    isPatientAdmittedAt[p,d] = false
  )
));

