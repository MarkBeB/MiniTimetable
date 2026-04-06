include "../IHTP-Model.mzn";

% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%

% Slight regression over v2

array[NurseIds, Days, ShiftTypes] of var 0..: nurseExcessiveWorkload;
array[RoomIds, Days, ShiftTypes] of var 0..: roomWorkload;
array[NurseIds, Days, ShiftTypes] of var 0..: nurseWorkload;

constraint forall(r in RoomIds, d in Days, s in ShiftTypes) (
  roomWorkload[r,d,s] = (
    sum(p in PatientIds where r in compatibleRooms(p)) (
      let {
        var int: workload = sum(start in validAdmissionDays[p] where start <= d /\ d < start + patientLengthOfStay(p)) (
          (patientAdmissionDay[p] == start) * patientWorkload(p,d,s,start)
        )
      } in 
      (isPatientPresent[p,d] /\ isPatientInRoom[p,r]) * workload
    )
    + sum(o in OccupantIds where isOccupantInRoom(o,d,r))(occupantWorkload(o,d,s))
  ) :: "Con_S2_A \(r) \(d) \(s)"
);

constraint forall(n in NurseIds, d in Days, s in ShiftTypes where n in activeNurses[d, s]) (
  nurseWorkload[n,d,s] = sum(r in RoomIds) (
    isNurseInRoom[n,r,d,s] * roomWorkload[r, d, s]
  )
  :: "Con_S2_B \(n) \(d) \(s)"
);

/*
constraint forall(n in NurseIds, d in Days, s in ShiftTypes where n in activeNurses[d,s]) (
  nurseExcessiveWorkload[n,d,s] >= nurseWorkload[n,d,s] - nurseWorkloadCapacity(n,d,s)
  :: "Con_S2_C \(n) \(d) \(s)"
);
*/

constraint forall(n in NurseIds, d in Days, s in ShiftTypes) (
  if n in activeNurses[d,s] then
    nurseExcessiveWorkload[n,d,s] == max(0, nurseWorkload[n,d,s] - nurseWorkloadCapacity(n,d,s))
  else
    nurseExcessiveWorkload[n,d,s] == 0
  endif
  :: "Con_S2_C \(n) \(d) \(s)"
);

constraint penaltyNurseEccessiveWorkload = Weights.nurseEccessiveWorkload * (
  sum(n in NurseIds, d in Days, s in ShiftTypes)(nurseExcessiveWorkload[n,d,s])
) :: "Con_S4_O";