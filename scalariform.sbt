import scalariform.formatter.preferences._
 
scalariformSettings
 
ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(SpaceInsideBrackets, true)
  .setPreference(SpaceInsideParentheses, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 160)
