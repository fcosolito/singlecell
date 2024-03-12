use("singlecell");

cellId = "exp11"
experimentId = "exp1"
resolutionId = "exp1cluster_0.10"
clusterId = "exp1cluster_0.101"
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1", "Gata1", "Naaa"];
db.cluster.aggregate([ 
  {
    $lookup: { 
      from:"resolution",
      localField:"resolution.$id",
      foreignField:"_id",
      as:"resolutionInfo"
    }
  },
  {
    $match:{
      "resolutionInfo.experiment.$id":"exp1"
    }
  }
])
/* */