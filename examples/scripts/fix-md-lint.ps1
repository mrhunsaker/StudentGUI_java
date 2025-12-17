# Fix common Markdown lint issues across the repo
# - Replace incorrect closing fences "```text" with "```"
# - Convert angle-bracketed URLs like <https://...> into explicit markdown links [https://...](https://...)
# - Make a backup copy of each file with extension .bak before changing

$mdFiles = Get-ChildItem -Path . -Filter *.md -Recurse -File
Write-Host "Found $($mdFiles.Count) markdown files"

foreach ($f in $mdFiles) {
    $text = Get-Content -Raw -LiteralPath $f.FullName
    $orig = $text

    # Replace ```text with ```
    $text = $text -replace "```text","```"

    # Convert angle-bracketed URLs to markdown explicit links: <https://...> => [https://...](https://...)
    $text = [regex]::Replace($text, '<(https?://[^>\s]+)>', '[$1]($1)')

    if ($text -ne $orig) {
        $bak = $f.FullName + '.bak'
        Copy-Item -LiteralPath $f.FullName -Destination $bak -Force
        Set-Content -LiteralPath $f.FullName -Value $text -Encoding UTF8
        Write-Host $f.FullName
    }
}

Write-Host "Done. Backups saved as *.md.bak where files were changed."