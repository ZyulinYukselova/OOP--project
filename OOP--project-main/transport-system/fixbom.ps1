$path = "src/main/java/com/transport/ticketing/cli/Cli.java"
$text = Get-Content -LiteralPath $path -Raw
$enc = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($path, $text, $enc)
