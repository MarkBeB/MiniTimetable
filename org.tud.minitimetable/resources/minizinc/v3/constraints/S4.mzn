include "../IHTP-model.mzn";

% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%

constraint forall(r in RoomIds, d in Days, s in ShiftTypes)(
  let {
    int: occupantWorkload = sum(o in OccupantIds where isOccupantInRoom(o, d, r))
      (occupantWorkload(o,d,s));
    var int: patientWorkload = sum(p in PatientIds where patientVar[p].assignedRoom[d] == r)
      (patientData[p].workloadProduced[enum2int(s) - 1 + (enum2int(d)-patientVar[p].admissionDay) * length(ShiftTypes)]) 
  } in
  roomWorkload[r,d,s] = occupantWorkload + patientWorkload  
);

constraint Penalties.nurseEccessiveWorkload = Weights.nurseEccessiveWorkload * sum(n in NurseIds, d in Days, s in ShiftTypes where isNurseAvailable(n,d,s))(
  let { var 0..1000: workload = sum(r in RoomIds where isNurseInRoom[n,d,s,r])(roomWorkload[r,d,s]) } in
  max(0, workload - nurseWorkloadCapacity(n,d,s))
);