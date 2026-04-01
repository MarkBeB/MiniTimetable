include "../IHTP-model.mzn";

% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%

array[RoomIds, Days, ShiftTypes] of var 0..10000: occupantWorkload = [
  (r,d,s): sum(o in OccupantIds where isOccupantPresentInRoom(o,d,r))(occupantWorkload(o,d,s)) 
  | r in RoomIds, d in Days, s in ShiftTypes
];

array[PatientIds, Days, ShiftTypes, Days] of int: precomputedWorkload = array4d(PatientIds, Days, ShiftTypes, Days, [
  (p,d,s,k):
  let{ int: relativeDay = d - k } in
  if relativeDay >= 0 /\ relativeDay < patientLengthOfStay(p) then
    %patientData[p].workloadProduced[ ((enum2int(s)-1) + relativeDay * card(ShiftTypes)) ]
    patientWorkload(p, relativeDay, s)
  else
    0
  endif
  | p in PatientIds, d in Days, s in ShiftTypes, k in Days
]);

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  roomWorkload[r,d,s] = occupantWorkload[r,d,s] + sum(p in PatientIds, k in Days)(
    (isPatientPresentInRoom[p,d,r] /\ (patientAdmissionDay[p] == k)) * precomputedWorkload[p, d, s, k]
  )
);

constraint Penalties.nurseEccessiveWorkload = Weights.nurseEccessiveWorkload * 
  sum(n in NurseIds, d in Days, s in ShiftTypes where isNurseAvailable(n,d,s))(
    let { 
      var 0..10000: workload = sum(r in RoomIds)(isNursePresentInRoom[n,d,s,r] * roomWorkload[r,d,s]) 
    } in 
    max(0, workload - nurseWorkloadCapacity(n,d,s))
  )


/*
array[RoomIds, Days] of var 0..10000: pload;

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  let {
    int: occupantWorkload = sum(o in OccupantIds where occupantData[o].assignedRoom == r /\ d < occupantData[o].lengthOfStay)
      (occupantData[o].workloadProduced[enum2int(s) - 1 + enum2int(d) * length(ShiftTypes)]);
    var int: patientWorkload = sum(p in PatientIds where isPatientPresentInRoom[p,d,r])
      (patientData[p].workloadProduced[enum2int(s) - 1 + (enum2int(d)-patientAdmissionDay[p]) * length(ShiftTypes)]) 
  } in
  roomWorkload[r,d,s] = occupantWorkload + patientWorkload  
);

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  let {
    var int: patientWorkload = sum(p in PatientIds where isPatientPresentInRoom[p,d,r])
      (patientData[p].workloadProduced[enum2int(s) - 1 + (enum2int(d)-patientAdmissionDay[p]) * length(ShiftTypes)]) 
  } in
  roomWorkload[r,d,s] = patientWorkload  
);
*/

%constraint Penalties.nurseEccessiveWorkload = Weights.nurseEccessiveWorkload * sum(nurseId in NurseIds, day in Days, shift in ShiftTypes where nurseVar[nurseId].isWorking[day, shift])(
%  let { var 0..1000: workload = sum(roomId in RoomIds where nurseVar[nurseId].assignedRooms[day, shift, roomId])(roomVar[roomId].workload[day, shift]) } in
%  max(0, workload - nurseData[nurseId].maxWorkloadPerShift[day, shift])
%);

%constraint forall(r in RoomIds, d in Days)(
%  pload[r,d] = sum(p in PatientIds where isPatientPresentInRoom[p,d,r])(sum(s in ShiftTypes)(patientWorkload(p,d,s)))
%);

/* 
constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  let {
    int: occupantWorkload = sum(o in OccupantIds where isOccupantPresentInRoom(o,d,r))(occupantWorkload(o,d,s));
    var int: patientWorkload = sum(p in PatientIds where isPatientPresentInRoom[p,d,r])(patientWorkload(p,d,s));
  } in
  wload[r,d,s] = occupantWorkload + patientWorkload  
);

constraint Penalties.nurseEccessiveWorkload = Weights.nurseEccessiveWorkload * 
  sum(n in NurseIds, d in Days, s in ShiftTypes where isNurseAvailable(n,d,s))(
    let { 
      var 0..10000: workload = sum(r in RoomIds where isNursePresentInRoom[n,d,s,r])(roomWorkload[r,d,s]) 
    } in 
    max(0, workload - nurseWorkloadCapacity(n,d,s))
  )
*/