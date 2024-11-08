procedure InitializeWizard();
var
    defaultPath: String;
    currentPath: String;
begin
    defaultPath := ExpandConstant('{localappdata}\RuneLite');
    { this defaults to the current installed location read from the registry }
    currentPath := GetWizardForm.DirEdit.Text;
    if defaultPath <> currentPath then begin
        if not DirExists(currentPath) then begin
            { Already installed to a non-default location which doesn't exist.
              It is not possible to make the installer reenable the dir page here,
              but we can at least reset the install location. }
            Log(Format('Resetting diredit path to %s', [defaultPath]))
            GetWizardForm.DirEdit.Text := defaultPath
        end
    end
end;