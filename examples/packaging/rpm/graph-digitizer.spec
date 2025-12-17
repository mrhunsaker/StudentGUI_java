Name:           graph-digitizer
Version:        1.2.0
Release:        1%{?dist}
Summary:        Extract numeric data from plotted graphs (JavaFX)
License:        Apache-2.0
URL:            https://github.com/mrhunsaker/Graph_Digitizer_Java_Implementation
Source0:        %{name}-%{version}.tar.gz

BuildRequires:  java-21-openjdk-devel
Requires:       java-21-openjdk

%description
Graph Digitizer is a JavaFX desktop application that allows users to load
raster images of graphs and extract numeric data points.

%prep
%setup -q

%build
# Build via Maven producing jpackage app-image (expects source layout)
mvn -B -Pnative -Djpackage.type=app-image package

%install
# Install under /opt/graph-digitizer
mkdir -p %{buildroot}/opt/graph-digitizer
cp -r graph-digitizer-java/target/GraphDigitizer/* %{buildroot}/opt/graph-digitizer/
# Desktop file
install -D -m 0644 graph-digitizer-java/packaging/graph-digitizer.desktop %{buildroot}/usr/share/applications/graph-digitizer.desktop
# Icon (256px)
install -D -m 0644 graph-digitizer-java/build/icons/scatter-plot-256.png %{buildroot}/usr/share/icons/hicolor/256x256/apps/graph-digitizer.png

%post
if [ -x /usr/bin/update-desktop-database ]; then /usr/bin/update-desktop-database -q || true; fi
if [ -x /usr/bin/gtk-update-icon-cache ]; then /usr/bin/gtk-update-icon-cache -q /usr/share/icons/hicolor || true; fi

%postun
if [ $1 -eq 0 ]; then
  if [ -x /usr/bin/update-desktop-database ]; then /usr/bin/update-desktop-database -q || true; fi
  if [ -x /usr/bin/gtk-update-icon-cache ]; then /usr/bin/gtk-update-icon-cache -q /usr/share/icons/hicolor || true; fi
fi

%files
/opt/graph-digitizer
/usr/share/applications/graph-digitizer.desktop
/usr/share/icons/hicolor/256x256/apps/graph-digitizer.png

%changelog
* Tue Nov 18 2025 Maintainer <maintainer@example.com> - 1.2.0-1
- Initial RPM spec template
