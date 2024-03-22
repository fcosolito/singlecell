use("singlecell");

experimentId = "exp1"
resolutionId = ObjectId("65fc35eb87c9af7b37edbfcf")
clusterId = ObjectId("65fc35eb87c9af7b37edbfd9")
cellIds = [ObjectId("65fc35eb87c9af7b37edc001"), ObjectId("65fc35eb87c9af7b37edc002"), ObjectId("65fc35eb87c9af7b37edc003")]
numberOfMarkers = 5
geneCodes = ["Vcpip1", "Tram1", "Gata1", "Naaa", "Casp1"]
result = db.getCollection("geneExpressionList").aggregate([
    {
      $match: {
        "experiment.$id": experimentId,
        "code": {
          $in: geneCodes
      }
      }
    },
    {
      $unwind: "$expressions"
    },
    {
      $lookup: {
        from: "cell",
        localField: "expressions.cell.$id",
        foreignField: "_id",
        as: "cellInfo"
        }
    },
    {
      $project: {
      experiment:1,
      code:1,
      expression: "$expressions.expression",
      barcode: "$cellInfo.barcode"
        
      }
    },
    {
      $group: {
        _id: {
          experiment:"$experiment",
          code: "$code"
        },
        expressions: {
          $push: {
            barcode: "$barcode",
            expression: "$expression"
          }
        }
    }
  }
    
  ])/*
/* */
print(result);
