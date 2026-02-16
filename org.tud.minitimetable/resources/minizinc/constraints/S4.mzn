include "../data/IHTC_model.mzn";

% NRA
% S4 Maximum workload: For each shift, the total workload induced by patients staying in rooms assigned to a nurse
% should not exceed the maximum workload of the nurse for this shift. 
% The penalty is the amount by which the total workload exceeds the limit, or 0 if it does not exceed the maximum workload of the nurse.
%

constraint forall(roomId in RoomIds, day in Days, shift in ShiftTypes)(
  let {
    int: occupantWorkload = sum(occupant in occupantData where occupant.assignedRoom == roomId /\ day < occupant.lengthOfStay)
      (occupant.workloadProduced[enum2int(shift) - 1 + enum2int(day) * length(ShiftTypes)]);
    var int: patientWorkload = sum(patientId in PatientIds where patientVar[patientId].assignedRoom[day] == roomId)
      (patientData[patientId].workloadProduced[enum2int(shift) - 1 + (enum2int(day)-patientVar[patientId].admissionDay) * length(ShiftTypes)]) 
  } in
  roomVar[roomId].workload[day, shift] = occupantWorkload + patientWorkload  
);

constraint Penalties.nurseEccessiveWorkload = Weights.nurseEccessiveWorkload * sum(nurseId in NurseIds, day in Days, shift in ShiftTypes where nurseVar[nurseId].isWorking[day, shift])(
  let { var 0..1000: workload = sum(roomId in RoomIds where nurseVar[nurseId].assignedRooms[day, shift, roomId])(roomVar[roomId].workload[day, shift]) } in
  max(0, workload - nurseData[nurseId].maxWorkloadPerShift[day, shift])
);