include "IHTC_model.mzn";
include "globals.mzn";

constraint forall(day in Day, room in rooms)(
  card(room.accommodatedPatientsPerDay[day]) <= room.availableCapacity[day]
);

constraint forall(day in Day)(
    forall(roomA, roomB in rooms where roomA != roomB)(
      disjoint(roomA.accommodatedPatientsPerDay[day], roomB.accommodatedPatientsPerDay[day])
    )
);

/*
constraint forall(day in Day, room in rooms)(
  forall(id in room.accommodatedPatientsPerDay[day])(
    patients[id].assignedToRoom != <>
  )
);
*/

/*
constraint forall(room in rooms, day in Day)(
  room.agePenalty[day] == max(patientId in room.accommodatedPatientsPerDay[day])( patients[patientId].ageGroup ) -  min(patientId in room.accommodatedPatientsPerDay[day])( patients[patientId].ageGroup )
);
*/

/*
var int: penalty;
constraint penalty == sum(room in rooms, day in Day)(
  enum2int(max(patientId in room.accommodatedPatientsPerDay[day])( patients[patientId].ageGroup )) -  
  enum2int(min(patientId in room.accommodatedPatientsPerDay[day])( patients[patientId].ageGroup ))
);
*/

% solve minimize penalty;
% output ["penalty = \(penalty)\n\n"]