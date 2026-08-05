include "../IHTP-Model.mzn";

% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%

array[RoomIds, Days, ShiftTypes] of var 0..: roomWorkload;
array[NurseIds, Days, ShiftTypes] of var 0..: nurseWorkload;
array[NurseIds, Days, ShiftTypes] of var 0..: nurseExcessiveWorkload;

% build linear terms to compute the workload for every room-day-shift combination.
constraint forall(r in RoomIds, d in Days, s in ShiftTypes) (
  roomWorkload[r,d,s] = (
    sum(p in PatientIds where r in compatibleRooms(p)) (
      let {
        var int: workload = sum(da in validAdmissionDays(p) where da <= d /\ d < da + patientLengthOfStay(p)) (
          isPatientAdmittedAt[p,da] * patientWorkload(p,d,s,da)
        )
      } in 
      isPatientPresentInRoom[p,r,d] * workload
    )
    + sum(o in OccupantIds where isOccupantInRoom(o,d,r))(occupantWorkload(o,d,s))
  ) :: "Con_S2_A \(r) \(d) \(s)"
);

% build linear terms to compute the total nurse workload across all rooms for a given day-shift combination
constraint forall(n in NurseIds, d in Days, s in ShiftTypes where n in availableNurses(d,s)) (
  nurseWorkload[n,d,s] = sum(r in RoomIds) (
    isNurseInRoom[n,r,d,s] * roomWorkload[r, d, s]
  )
  :: "Con_S2_B \(n) \(d) \(s)"
);

% concrete difference between workload and workload capacity
constraint forall(n in NurseIds, d in Days, s in ShiftTypes) (
  if n in availableNurses(d,s) then
    nurseExcessiveWorkload[n,d,s] == max(0, nurseWorkload[n,d,s] - nurseWorkloadCapacity(n,d,s))
  else
    nurseExcessiveWorkload[n,d,s] == 0
  endif
  :: "Con_S2_C \(n) \(d) \(s)"
);

constraint violationsNurseEccessiveWorkload = sum(n in NurseIds, d in Days, s in ShiftTypes)(nurseExcessiveWorkload[n,d,s]) :: "Con_S4_O";
