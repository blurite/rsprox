// https://stackoverflow.com/a/61522649
function EndsWith(SubText, Text: string): Boolean;
var
  EndStr: string;
begin
  EndStr := Copy(Text, Length(Text) - Length(SubText) + 1, Length(SubText));
  { Use SameStr, if you need a case-sensitive comparison }
  Result := SameText(SubText, EndStr);
end;

function InitializeSetup(): Boolean;
var
  username: String;
begin
  username := GetUserNameString();
  Result := True;
  if EndsWith('!', username) then
  begin
    MsgBox('RuneLite is incompatible with Windows usernames which end with an exclamation mark (!).',
      mbError, MB_OK);
    Result := False;
  end;
end;